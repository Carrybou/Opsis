package altn72.opsis.exception;

public class RessourceIntrouvableException extends RuntimeException {

    public RessourceIntrouvableException(String ressource, Long id) {
        super(ressource + " introuvable (identifiant " + id + ")");
    }
}