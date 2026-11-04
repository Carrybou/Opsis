package altn72.opsis.exception.introuvable;

public class StationIntrouvableException extends RessourceIntrouvableException {

    public StationIntrouvableException(Long idStation) {
        super("La station n°" + idStation + " est introuvable");
    }
}
