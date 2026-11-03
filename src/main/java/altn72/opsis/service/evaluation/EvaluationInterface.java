package altn72.opsis.service.evaluation;

import altn72.opsis.dto.evaluation.EvaluationRequest;
import altn72.opsis.dto.evaluation.EvaluationResponse;
import altn72.opsis.security.UtilisateurConnecte;

import java.util.Map;

public interface EvaluationInterface {

    EvaluationResponse evaluer(Long idIncident, EvaluationRequest request, UtilisateurConnecte utilisateur);

    boolean estEvaluable(Long idIncident, UtilisateurConnecte utilisateur);

    Map<Long, Integer> notesParIncident();
}
