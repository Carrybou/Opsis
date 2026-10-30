package altn72.opsis.service.incident;

import altn72.opsis.dto.incident.IncidentDetailResponse;
import altn72.opsis.dto.incident.IncidentModificationRequest;
import altn72.opsis.dto.incident.IncidentRequest;
import altn72.opsis.dto.incident.IncidentResponse;
import altn72.opsis.exception.EquipementIntrouvableException;
import altn72.opsis.exception.IncidentIntrouvableException;
import altn72.opsis.exception.ModificationIncidentInterditeException;
import altn72.opsis.exception.UtilisateurIntrouvableException;
import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.equipement.EquipementEtat;
import altn72.opsis.model.evaluation.Evaluation;
import altn72.opsis.model.evaluation.EvaluationRepository;
import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.utilisateur.Utilisateur;
import altn72.opsis.model.equipement.EquipementRepository;
import altn72.opsis.model.incident.IncidentRepository;
import altn72.opsis.model.utilisateur.UtilisateurRepository;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.UtilisateurConnecte;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IncidentService implements IncidentInterface {

    private final IncidentRepository incidentRepository;
    private final EquipementRepository equipementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EvaluationRepository evaluationRepository;

    public IncidentService(IncidentRepository incidentRepository,
                           EquipementRepository equipementRepository,
                           UtilisateurRepository utilisateurRepository,
                           EvaluationRepository evaluationRepository) {
        this.incidentRepository = incidentRepository;
        this.equipementRepository = equipementRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.evaluationRepository = evaluationRepository;
    }

    @Override
    @Transactional
    public IncidentResponse declarer(IncidentRequest request, Long idUtilisateur) {
        Equipement equipement = equipementRepository.findById(request.getIdEquipement())
                .orElseThrow(() -> new EquipementIntrouvableException(request.getIdEquipement()));

        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new UtilisateurIntrouvableException(idUtilisateur));

        Incident incident = new Incident();
        incident.setDescription(request.getDescription().trim());
        incident.setDateHeureOuverture(LocalDateTime.now());
        incident.setStatut(IncidentStatut.OUVERT);
        incident.setDateHeureResolution(null);
        incident.setIdEquipement(equipement);
        incident.setIdPrestataire(null);
        incident.setIdUtilisateur(utilisateur);
        equipement.setEtatFonctionnement(EquipementEtat.EN_PANNE);

        return IncidentResponse.from(incidentRepository.save(incident));
    }

    @Override
    @Transactional(readOnly = true)
    public IncidentDetailResponse consulterDetail(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentIntrouvableException(id));

        Evaluation evaluation = evaluationRepository.findByIdIncident_Id(id).orElse(null);

        return IncidentDetailResponse.from(incident, evaluation);
    }

    // Transactionnel pour que la modification de l'entité chargée soit enregistrée en fin de méthode.
    @Override
    @Transactional
    public IncidentResponse modifier(Long idIncident, IncidentModificationRequest request, UtilisateurConnecte utilisateur) {
        Incident incident = chargerModifiable(idIncident, utilisateur);
        incident.setDescription(request.getDescription().trim());
        return IncidentResponse.from(incident);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estModifiable(Long idIncident, UtilisateurConnecte utilisateur) {
        Incident incident = incidentRepository.findById(idIncident)
                .orElseThrow(() -> new IncidentIntrouvableException(idIncident));
        return motifRefusModification(incident, utilisateur).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> incidentsModifiables(Long idEquipement, UtilisateurConnecte utilisateur) {
        return incidentRepository.findByIdEquipement_IdOrderByDateHeureOuvertureDesc(idEquipement).stream()
                .filter(incident -> motifRefusModification(incident, utilisateur).isEmpty())
                .map(Incident::getId)
                .collect(Collectors.toSet());
    }

    private Incident chargerModifiable(Long idIncident, UtilisateurConnecte utilisateur) {
        Incident incident = incidentRepository.findById(idIncident)
                .orElseThrow(() -> new IncidentIntrouvableException(idIncident));
        motifRefusModification(incident, utilisateur).ifPresent(motif -> {
            throw new ModificationIncidentInterditeException(motif);
        });
        return incident;
    }

    // F6 : le responsable modifie tout incident non résolu ; l'agent seulement ceux qu'il a déclarés,
    // tant qu'aucun prestataire ne les a pris en charge. Un incident résolu est figé (RG5).
    private Optional<String> motifRefusModification(Incident incident, UtilisateurConnecte utilisateur) {
        if (incident.estResolu()) {
            return Optional.of("L'incident n°" + incident.getId() + " est résolu : il ne peut plus être modifié");
        }
        if (utilisateur.aUnProfil(UtilisateurType.ADMIN)) {
            return Optional.empty();
        }
        if (!utilisateur.aUnProfil(UtilisateurType.AGENT)) {
            return Optional.of("Votre profil ne permet pas de modifier un incident");
        }
        if (!incident.estDeclarePar(utilisateur.id())) {
            return Optional.of("Vous ne pouvez modifier que les incidents que vous avez déclarés");
        }
        if (incident.estAffecte()) {
            return Optional.of("L'incident n°" + incident.getId()
                    + " est déjà affecté à un prestataire : seul un responsable d'exploitation peut le modifier");
        }
        return Optional.empty();
    }
}
