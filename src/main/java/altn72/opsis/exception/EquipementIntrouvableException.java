package altn72.opsis.exception;

public class EquipementIntrouvableException extends RessourceIntrouvableException {

    public EquipementIntrouvableException(Long idEquipement) {
        super("L'équipement n°" + idEquipement + " est introuvable");
    }
}
