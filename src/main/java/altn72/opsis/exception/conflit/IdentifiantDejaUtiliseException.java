package altn72.opsis.exception.conflit;

public class IdentifiantDejaUtiliseException extends RuntimeException {

    public IdentifiantDejaUtiliseException(String identifiantConnexion) {
        super("L'identifiant " + identifiantConnexion + " est déjà utilisé");
    }
}