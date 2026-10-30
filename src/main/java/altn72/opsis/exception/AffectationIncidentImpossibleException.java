package altn72.opsis.exception;

// F7 : l'état de l'incident ne permet pas l'affectation demandée (incident résolu, prestataire déjà affecté).
public class AffectationIncidentImpossibleException extends RuntimeException {

    public AffectationIncidentImpossibleException(String motif) {
        super(motif);
    }
}
