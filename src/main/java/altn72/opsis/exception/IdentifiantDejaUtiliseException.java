package altn72.opsis.exception;

public class IdentifiantDejaUtiliseException extends RuntimeException {

    public IdentifiantDejaUtiliseException(String identifiantConnexion) {
        super("L'identifiant " + identifiantConnexion + " est déjà utilisé");
    }
}