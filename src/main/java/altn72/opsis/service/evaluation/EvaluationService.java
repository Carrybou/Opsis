package altn72.opsis.service.evaluation;

import altn72.opsis.dto.evaluation.EvaluationRequest;
import altn72.opsis.dto.evaluation.EvaluationResponse;
import altn72.opsis.exception.AccesRefuseException;
import altn72.opsis.exception.EvaluationImpossibleException;
import altn72.opsis.exception.IncidentIntrouvableException;
import altn72.opsis.model.evaluation.Evaluation;
import altn72.opsis.model.evaluation.EvaluationRepository;
import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentRepository;
import altn72.opsis.security.UtilisateurConnecte;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EvaluationService implements EvaluationInterface {

    private final EvaluationRepository evaluationRepository;
    private final IncidentRepository incidentRepository;

    public EvaluationService(EvaluationRepository evaluationRepository, IncidentRepository incidentRepository) {
        this.evaluationRepository = evaluationRepository;
        this.incidentRepository = incidentRepository;
    }

    @Override
    @Transactional
    public EvaluationResponse evaluer(Long idIncident, EvaluationRequest request, UtilisateurConnecte utilisateur) {
        if (!utilisateur.peutEvaluerPrestataire()) {
            throw new AccesRefuseException("Seul un responsable d'exploitation peut évaluer un prestataire");
        }
        Incident incident = incidentRepository.findById(idIncident)
                .orElseThrow(() -> new IncidentIntrouvableException(idIncident));
        motifRefusEvaluation(incident).ifPresent(motif -> {
            throw new EvaluationImpossibleException(motif);
        });

        Evaluation evaluation = new Evaluation();
        evaluation.setNote(request.getNote());
        evaluation.setCommentaire(commentaireNettoye(request.getCommentaire()));
        evaluation.setDate(LocalDate.now());
        evaluation.setIdIncident(incident);
        return EvaluationResponse.from(evaluationRepository.save(evaluation));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estEvaluable(Long idIncident, UtilisateurConnecte utilisateur) {
        Incident incident = incidentRepository.findById(idIncident)
                .orElseThrow(() -> new IncidentIntrouvableException(idIncident));
        return utilisateur.peutEvaluerPrestataire() && motifRefusEvaluation(incident).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Integer> notesParIncident() {
        return evaluationRepository.findAll().stream()
                .collect(Collectors.toMap(evaluation -> evaluation.getIdIncident().getId(), Evaluation::getNote));
    }

    // RG8 : une évaluation concerne un incident résolu et affecté à un prestataire, et il n'y en a qu'une par incident.
    private Optional<String> motifRefusEvaluation(Incident incident) {
        if (!incident.estResolu()) {
            return Optional.of("L'incident n°" + incident.getId() + " n'est pas encore résolu : il ne peut pas être évalué");
        }
        if (incident.getIdPrestataire() == null) {
            return Optional.of("L'incident n°" + incident.getId() + " n'a été affecté à aucun prestataire : il n'y a pas d'intervention à évaluer");
        }
        if (evaluationRepository.existsByIdIncident_Id(incident.getId())) {
            return Optional.of("L'incident n°" + incident.getId() + " a déjà été évalué");
        }
        return Optional.empty();
    }

    private static String commentaireNettoye(String commentaire) {
        return commentaire == null || commentaire.isBlank() ? null : commentaire.trim();
    }
}
