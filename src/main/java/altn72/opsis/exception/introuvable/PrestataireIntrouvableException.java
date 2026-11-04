package altn72.opsis.exception.introuvable;

public class PrestataireIntrouvableException extends RessourceIntrouvableException {

    public PrestataireIntrouvableException(Long idPrestataire) {
        super("Le prestataire n°" + idPrestataire + " est introuvable");
    }
}
