package altn72.opsis.service.equipement;

import altn72.opsis.dto.equipement.EquipementResponse;
import altn72.opsis.dto.equipement.FicheEquipementResponse;

public interface EquipementInterface {

    FicheEquipementResponse consulterFiche(Long idEquipement);

    EquipementResponse trouver(Long id);

}
