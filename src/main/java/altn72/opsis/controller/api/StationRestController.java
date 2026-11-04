package altn72.opsis.controller.api;

import altn72.opsis.dto.station.StationResponse;
import altn72.opsis.service.station.StationInterface;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationRestController {

    private final StationInterface stationService;

    public StationRestController(StationInterface stationService) {
        this.stationService = stationService;
    }

    @GetMapping
    public List<StationResponse> lister() {
        return stationService.lister();
    }

    @GetMapping("/{id}")
    public StationResponse consulter(@PathVariable Long id) {
        return stationService.trouver(id);
    }
}
