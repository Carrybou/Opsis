package altn72.opsis.service.equipement;

import altn72.opsis.dto.FicheEquipementResponse;

public interface EquipementInterface {

    FicheEquipementResponse consulterFiche(Long idEquipement);
}
