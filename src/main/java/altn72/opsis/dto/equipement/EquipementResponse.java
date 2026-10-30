package altn72.opsis.dto.equipement;

import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.equipement.EquipementEtat;
import altn72.opsis.model.equipement.EquipementType;

public record EquipementResponse(
        Long identifiant,
        String referenceUnique,
        String libelleDescription,
        EquipementType type,
        EquipementEtat etatFonctionnement,
        String nomStation
) {
    public static EquipementResponse from(Equipement equipement) {
        return new EquipementResponse(
                equipement.getId(),
                equipement.getReferenceUnique(),
                equipement.getLibelleDescription(),
                equipement.getType(),
                equipement.getEtatFonctionnement(),
                equipement.getIdStation().getNom()
        );
    }
}