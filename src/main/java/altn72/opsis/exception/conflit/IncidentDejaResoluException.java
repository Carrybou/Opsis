package altn72.opsis.exception.conflit;

public class IncidentDejaResoluException extends RuntimeException {

    public IncidentDejaResoluException(Long id) {
        super("L'incident n°" + id + " est déjà résolu");
    }
}