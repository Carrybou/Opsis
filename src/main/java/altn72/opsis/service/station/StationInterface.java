package altn72.opsis.service.station;

import altn72.opsis.dto.station.ReseauStationsResponse;
import altn72.opsis.dto.station.StationResponse;

import java.util.List;

public interface StationInterface {

    List<StationResponse> lister();

    StationResponse trouver(Long id);

    ReseauStationsResponse consulterReseau();
}
