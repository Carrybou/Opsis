package altn72.opsis.dto;

import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.equipement.EquipementEtat;
import altn72.opsis.model.equipement.EquipementType;
import altn72.opsis.model.station.Station;

import java.util.List;

public record FicheEquipementResponse(
        Long identifiant,
        String referenceUnique,
        String libelleDescription,
        EquipementType type,
        EquipementEtat etatFonctionnement,
        Long idStation,
        String nomStation,
        List<IncidentResume> incidentsEnCours,
        List<IncidentResume> incidentsPasses
) {
    public static FicheEquipementResponse from(Equipement equipement,
                                               List<IncidentResume> incidentsEnCours,
                                               List<IncidentResume> incidentsPasses) {
        Station station = equipement.getIdStation();
        return new FicheEquipementResponse(
                equipement.getId(),
                equipement.getReferenceUnique(),
                equipement.getLibelleDescription(),
                equipement.getType(),
                equipement.getEtatFonctionnement(),
                station.getId(),
                station.getNom(),
                incidentsEnCours,
                incidentsPasses
        );
    }
}
