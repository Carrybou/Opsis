package altn72.opsis.controller.web;

import altn72.opsis.service.station.StationInterface;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StationViewController {

    private final StationInterface stationService;

    public StationViewController(StationInterface stationService) {
        this.stationService = stationService;
    }

    @GetMapping("/stations")
    public String afficherStations(Model model) {
        model.addAttribute("reseau", stationService.consulterReseau());
        return "stations";
    }
}
