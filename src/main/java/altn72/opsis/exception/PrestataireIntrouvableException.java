package altn72.opsis.exception;

public class PrestataireIntrouvableException extends RessourceIntrouvableException {

    public PrestataireIntrouvableException(Long idPrestataire) {
        super("Le prestataire n°" + idPrestataire + " est introuvable");
    }
}
