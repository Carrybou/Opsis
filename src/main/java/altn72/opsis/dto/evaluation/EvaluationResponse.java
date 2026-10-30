package altn72.opsis.dto.evaluation;

import altn72.opsis.model.evaluation.Evaluation;

import java.time.LocalDate;

public record EvaluationResponse(
        Long identifiant,
        Integer note,
        String commentaire,
        LocalDate date
) {
    public static EvaluationResponse from(Evaluation evaluation) {
        return new EvaluationResponse(
                evaluation.getId(),
                evaluation.getNote(),
                evaluation.getCommentaire(),
                evaluation.getDate()
        );
    }
}