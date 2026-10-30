package altn72.opsis.exception;

public class UtilisateurIntrouvableException extends RessourceIntrouvableException {

    public UtilisateurIntrouvableException(Long idUtilisateur) {
        super("L'utilisateur n°" + idUtilisateur + " est introuvable");
    }
}
