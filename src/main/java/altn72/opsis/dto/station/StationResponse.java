package altn72.opsis.dto.station;

import altn72.opsis.model.station.Station;

import java.math.BigDecimal;

public record StationResponse(
        Long identifiant,
        String nom,
        BigDecimal latitude,
        BigDecimal longitude
) {
    public static StationResponse from(Station station) {
        return new StationResponse(
                station.getId(),
                station.getNom(),
                station.getLatitude(),
                station.getLongitude()
        );
    }
}
