package altn72.opsis.controller.api;

import altn72.opsis.dto.StationResponse;
import altn72.opsis.model.station.StationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationRestController {

    private final StationRepository stationRepository;

    public StationRestController(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @GetMapping
    public List<StationResponse> getStations() {
        return stationRepository.findAll().stream()
                .map(StationResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StationResponse> getStation(@PathVariable Long id) {
        return stationRepository.findById(id)
                .map(StationResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
