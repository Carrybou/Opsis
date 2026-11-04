package altn72.opsis.exception.introuvable;

public class EquipementIntrouvableException extends RessourceIntrouvableException {

    public EquipementIntrouvableException(Long idEquipement) {
        super("L'équipement n°" + idEquipement + " est introuvable");
    }
}
