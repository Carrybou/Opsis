package altn72.opsis.exception.acces;

public class IdentifiantsInvalidesException extends RuntimeException {

    public IdentifiantsInvalidesException() {
        super("Identifiant ou mot de passe incorrect");
    }
}