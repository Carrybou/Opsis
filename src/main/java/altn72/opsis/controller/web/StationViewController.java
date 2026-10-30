package altn72.opsis.controller.web;

import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.equipement.EquipementRepository;
import altn72.opsis.model.station.Station;
import altn72.opsis.model.station.StationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class StationViewController {

    private final StationRepository stationRepository;
    private final EquipementRepository equipementRepository;

    public StationViewController(StationRepository stationRepository, EquipementRepository equipementRepository) {
        this.stationRepository = stationRepository;
        this.equipementRepository = equipementRepository;
    }

    @GetMapping( "/stations")
    public String afficherStations(Model model) {
        List<Station> stations = stationRepository.findAll();
        List<Equipement> equipements = equipementRepository.findAll();
        Map<Long, List<Equipement>> equipementsParStation = equipements.stream()
                .collect(Collectors.groupingBy(equipement -> equipement.getIdStation().getId()));

        model.addAttribute("stations", stations);
        model.addAttribute("equipementsParStation", equipementsParStation);
        model.addAttribute("nombreStations", stations.size());
        model.addAttribute("nombreEquipements", equipements.size());
        model.addAttribute("nombreEnPanne", equipements.stream()
                .filter(equipement -> "EN_PANNE".equals(equipement.getEtatFonctionnement()))
                .count());
        model.addAttribute("nombreEnMaintenance", equipements.stream()
                .filter(equipement -> "EN_MAINTENANCE".equals(equipement.getEtatFonctionnement()))
                .count());
        return "stations";
    }
}