package altn72.opsis.dto.station;

import altn72.opsis.dto.equipement.EquipementResponse;

import java.util.List;
import java.util.Map;

public record ReseauStationsResponse(
        List<StationResponse> stations,
        Map<Long, List<EquipementResponse>> equipementsParStation,
        long nombreEquipements,
        long nombreEnPanne,
        long nombreEnMaintenance
) {
}
