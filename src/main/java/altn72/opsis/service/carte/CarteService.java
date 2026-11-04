package altn72.opsis.service.carte;

import altn72.opsis.dto.carte.CarteReseauResponse;
import altn72.opsis.dto.carte.CarteReseauResponse.EquipementCarte;
import altn72.opsis.dto.carte.CarteReseauResponse.IncidentCarte;
import altn72.opsis.dto.carte.CarteReseauResponse.StationCarte;
import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.equipement.EquipementEtat;
import altn72.opsis.model.equipement.EquipementRepository;
import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentRepository;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.station.Station;
import altn72.opsis.model.station.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CarteService implements CarteInterface {

    private final StationRepository stationRepository;
    private final EquipementRepository equipementRepository;
    private final IncidentRepository incidentRepository;

    public CarteService(StationRepository stationRepository, EquipementRepository equipementRepository,
                        IncidentRepository incidentRepository) {
        this.stationRepository = stationRepository;
        this.equipementRepository = equipementRepository;
        this.incidentRepository = incidentRepository;
    }

    // Une station sans coordonnées reste dans la réponse : elle n'a pas de marqueur, mais son panneau reste accessible depuis la liste.
    @Override
    @Transactional(readOnly = true)
    public CarteReseauResponse consulter() {
        Map<Long, List<Equipement>> equipementsParStation = equipementRepository.findAll().stream()
                .collect(Collectors.groupingBy(equipement -> equipement.getIdStation().getId()));
        Map<Long, List<Incident>> incidentsParEquipement = incidentRepository
                .findByStatutNotOrderByDateHeureOuvertureDesc(IncidentStatut.RESOLU).stream()
                .collect(Collectors.groupingBy(incident -> incident.getIdEquipement().getId()));

        List<StationCarte> stations = stationRepository.findAll().stream()
                .sorted(Comparator.comparing(Station::getNom))
                .map(station -> versStation(station, equipementsParStation.getOrDefault(station.getId(), List.of()), incidentsParEquipement))
                .toList();

        return new CarteReseauResponse(stations,
                compterStations(stations, EquipementEtat.EN_PANNE),
                compterStations(stations, EquipementEtat.EN_MAINTENANCE),
                compterStations(stations, EquipementEtat.FONCTIONNEL),
                compterStations(stations, null));
    }

    private StationCarte versStation(Station station, List<Equipement> equipements, Map<Long, List<Incident>> incidentsParEquipement) {
        List<EquipementCarte> equipementsCarte = equipements.stream()
                .sorted(Comparator.comparingInt((Equipement equipement) -> gravite(equipement.getEtatFonctionnement())).reversed()
                        .thenComparing(Equipement::getReferenceUnique))
                .map(equipement -> versEquipement(equipement, incidentsParEquipement.getOrDefault(equipement.getId(), List.of())))
                .toList();

        // Les équipements sont triés du plus grave au moins grave : le premier donne la situation de la station.
        EquipementEtat situation = equipementsCarte.isEmpty() ? null : equipementsCarte.getFirst().etat();

        return new StationCarte(station.getId(), station.getNom(), station.getLatitude(), station.getLongitude(), situation, equipementsCarte,
                compterEquipements(equipements, EquipementEtat.EN_PANNE),
                compterEquipements(equipements, EquipementEtat.EN_MAINTENANCE));
    }

    private EquipementCarte versEquipement(Equipement equipement, List<Incident> incidents) {
        return new EquipementCarte(equipement.getId(), equipement.getReferenceUnique(), equipement.getLibelleDescription(),
                equipement.getType(), equipement.getEtatFonctionnement(),
                incidents.stream()
                        .map(incident -> new IncidentCarte(incident.getId(), incident.getDescription(), incident.getStatut()))
                        .toList());
    }

    private static int gravite(EquipementEtat etat) {
        return switch (etat) {
            case EN_PANNE -> 2;
            case EN_MAINTENANCE -> 1;
            case FONCTIONNEL -> 0;
        };
    }

    private static long compterStations(List<StationCarte> stations, EquipementEtat situation) {
        return stations.stream().filter(station -> station.situation() == situation).count();
    }

    private static long compterEquipements(List<Equipement> equipements, EquipementEtat etat) {
        return equipements.stream().filter(equipement -> equipement.getEtatFonctionnement() == etat).count();
    }
}
