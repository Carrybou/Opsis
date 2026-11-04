package altn72.opsis.service.incident;

import altn72.opsis.dto.incident.AffectationRequest;
import altn72.opsis.dto.incident.IncidentDetailResponse;
import altn72.opsis.dto.incident.IncidentModificationRequest;
import altn72.opsis.dto.incident.IncidentRequest;
import altn72.opsis.dto.incident.IncidentResponse;
import altn72.opsis.exception.*;
import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.equipement.EquipementEtat;
import altn72.opsis.model.evaluation.Evaluation;
import altn72.opsis.model.evaluation.EvaluationRepository;
import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.prestataire.Prestataire;
import altn72.opsis.model.prestataire.PrestataireRepository;
import altn72.opsis.model.utilisateur.Utilisateur;
import altn72.opsis.model.equipement.EquipementRepository;
import altn72.opsis.model.incident.IncidentRepository;
import altn72.opsis.model.utilisateur.UtilisateurRepository;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.UtilisateurConnecte;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IncidentService implements IncidentInterface {

    private final IncidentRepository incidentRepository;
    private final EquipementRepository equipementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EvaluationRepository evaluationRepository;
    private final PrestataireRepository prestataireRepository;
    private final Clock horloge;

    public IncidentService(IncidentRepository incidentRepository,
                           EquipementRepository equipementRepository,
                           UtilisateurRepository utilisateurRepository,
                           EvaluationRepository evaluationRepository,
                           PrestataireRepository prestataireRepository,
                           Clock horloge) {
        this.incidentRepository = incidentRepository;
        this.equipementRepository = equipementRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.evaluationRepository = evaluationRepository;
        this.prestataireRepository = prestataireRepository;
        this.horloge = horloge;
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
        incident.setDateHeureOuverture(LocalDateTime.now(horloge));
        incident.setStatut(IncidentStatut.OUVERT);
        incident.setDateHeureResolution(null);
        incident.setEquipement(equipement);
        incident.setPrestataire(null);
        incident.setDeclarant(utilisateur);
        equipement.setEtatFonctionnement(EquipementEtat.EN_PANNE);

        return IncidentResponse.from(incidentRepository.save(incident));
    }

    @Override
    @Transactional(readOnly = true)
    public IncidentDetailResponse consulterDetail(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentIntrouvableException(id));

        Evaluation evaluation = evaluationRepository.findByIncident_Id(id).orElse(null);

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
        return incidentRepository.findByEquipement_IdOrderByDateHeureOuvertureDesc(idEquipement).stream()
                .filter(incident -> motifRefusModification(incident, utilisateur).isEmpty())
                .map(Incident::getId)
                .collect(Collectors.toSet());
    }

    // F7 / RG2 : affecter fait passer l'incident de "non affecté" à "affecté" ; réaffecter remplace le prestataire.
    // Le prestataire intervient : l'équipement passe en maintenance.
    // Transactionnel (RG10) : prestataire, statut et état de l'équipement sont enregistrés ensemble ou pas du tout.
    @Override
    @Transactional
    public IncidentResponse affecter(Long idIncident, AffectationRequest request, UtilisateurConnecte utilisateur) {
        if (!utilisateur.aUnProfil(UtilisateurType.ADMIN)) {
            throw new ModificationIncidentInterditeException("Seul un responsable d'exploitation peut affecter un prestataire");
        }
        Incident incident = incidentRepository.findById(idIncident)
                .orElseThrow(() -> new IncidentIntrouvableException(idIncident));
        // RG5 : un incident résolu ne peut plus être affecté ni réaffecté.
        if (incident.estResolu()) {
            throw new AffectationIncidentImpossibleException("L'incident n°" + idIncident
                    + " est résolu : il ne peut plus être affecté ni réaffecté");
        }
        Prestataire prestataire = prestataireRepository.findById(request.getIdPrestataire())
                .orElseThrow(() -> new PrestataireIntrouvableException(request.getIdPrestataire()));
        if (incident.estAffecteA(prestataire.getId())) {
            throw new AffectationIncidentImpossibleException("L'incident n°" + idIncident
                    + " est déjà affecté à " + prestataire.getNom());
        }

        incident.setPrestataire(prestataire);
        incident.setStatut(IncidentStatut.EN_COURS);
        incident.getEquipement().setEtatFonctionnement(EquipementEtat.EN_MAINTENANCE);
        return IncidentResponse.from(incident);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estAffectable(Long idIncident, UtilisateurConnecte utilisateur) {
        Incident incident = incidentRepository.findById(idIncident)
                .orElseThrow(() -> new IncidentIntrouvableException(idIncident));
        return utilisateur.aUnProfil(UtilisateurType.ADMIN) && !incident.estResolu();
    }

    private Incident chargerModifiable(Long idIncident, UtilisateurConnecte utilisateur) {
        Incident incident = incidentRepository.findById(idIncident)
                .orElseThrow(() -> new IncidentIntrouvableException(idIncident));
        motifRefusModification(incident, utilisateur).ifPresent(motif -> {
            throw new ModificationIncidentInterditeException(motif);
        });
        return incident;
    }

    @Override
    @Transactional
    public IncidentDetailResponse resoudre(Long id, UtilisateurConnecte utilisateur) {
        if (!utilisateur.peutResoudreIncident()) {
            throw new AccesRefuseException("Seul un administrateur ou un technicien peut déclarer un incident comme résolu");
        }

        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentIntrouvableException(id));

        if (incident.estResolu()) {
            throw new IncidentDejaResoluException(id);
        }

        incident.setStatut(IncidentStatut.RESOLU);
        incident.setDateHeureResolution(LocalDateTime.now(horloge));

        Equipement equipement = incident.getEquipement();
        equipement.setEtatFonctionnement(etatApresResolution(equipement.getId(), id));

        Evaluation evaluation = evaluationRepository.findByIncident_Id(id).orElse(null);
        return IncidentDetailResponse.from(incident, evaluation);
    }

    // L'état de l'équipement dépend des incidents qui restent : un prestataire encore à l'œuvre, une panne sans prestataire, ou plus rien.
    private EquipementEtat etatApresResolution(Long idEquipement, Long idIncidentResolu) {
        if (incidentRepository.existsByEquipement_IdAndStatutAndIdNot(idEquipement, IncidentStatut.EN_COURS, idIncidentResolu)) {
            return EquipementEtat.EN_MAINTENANCE;
        }
        if (incidentRepository.existsByEquipement_IdAndStatutNotAndIdNot(idEquipement, IncidentStatut.RESOLU, idIncidentResolu)) {
            return EquipementEtat.EN_PANNE;
        }
        return EquipementEtat.FONCTIONNEL;
    }

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

    @Override
    @Transactional(readOnly = true)
    public List<IncidentResponse> listerHistoriqueResolus() {
        return incidentRepository.findByStatutOrderByDateHeureResolutionDesc(IncidentStatut.RESOLU)
                .stream()
                .map(IncidentResponse::from)
                .toList();
    }
}
