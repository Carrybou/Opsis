package altn72.opsis.controller.api;

import altn72.opsis.dto.station.StationResponse;
import altn72.opsis.service.station.StationInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Stations")
@RestController
@RequestMapping("/api/stations")
public class StationRestController {

    private final StationInterface stationService;

    public StationRestController(StationInterface stationService) {
        this.stationService = stationService;
    }

    @Operation(summary = "Lister les stations", description = "Par ordre alphabétique, avec leurs coordonnées.")
    @ApiResponse(responseCode = "200", description = "Stations")
    @GetMapping
    public List<StationResponse> lister() {
        return stationService.lister();
    }

    @Operation(summary = "Consulter une station")
    @ApiResponse(responseCode = "200", description = "Station")
    @ApiResponse(responseCode = "404", description = "Station introuvable")
    @GetMapping("/{id}")
    public StationResponse consulter(@PathVariable Long id) {
        return stationService.trouver(id);
    }
}
