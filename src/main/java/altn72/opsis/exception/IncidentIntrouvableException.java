package altn72.opsis.exception;

public class IncidentIntrouvableException extends RessourceIntrouvableException {

    public IncidentIntrouvableException(Long idIncident) {
        super("L'incident n°" + idIncident + " est introuvable");
    }
}
