package altn72.opsis.exception;

// F10 / RG8 : l'état de l'incident ne permet pas de l'évaluer (non résolu, sans prestataire, déjà évalué).
public class EvaluationImpossibleException extends RuntimeException {

    public EvaluationImpossibleException(String motif) {
        super(motif);
    }
}
