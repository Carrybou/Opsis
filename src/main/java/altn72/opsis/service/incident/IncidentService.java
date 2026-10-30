package altn72.opsis.service.incident;

import altn72.opsis.dto.incident.IncidentDetailResponse;
import altn72.opsis.dto.incident.IncidentRequest;
import altn72.opsis.dto.incident.IncidentResponse;
import altn72.opsis.exception.EquipementIntrouvableException;
import altn72.opsis.exception.IncidentIntrouvableException;
import altn72.opsis.exception.RessourceIntrouvableException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

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
                .orElseThrow(() -> new EquipementIntrouvableException(idUtilisateur));

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
}