package altn72.opsis.service.station;

import altn72.opsis.dto.equipement.EquipementResponse;
import altn72.opsis.dto.station.ReseauStationsResponse;
import altn72.opsis.dto.station.StationResponse;
import altn72.opsis.exception.introuvable.StationIntrouvableException;
import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.equipement.EquipementEtat;
import altn72.opsis.model.equipement.EquipementRepository;
import altn72.opsis.model.station.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StationService implements StationInterface {

    private final StationRepository stationRepository;
    private final EquipementRepository equipementRepository;

    public StationService(StationRepository stationRepository, EquipementRepository equipementRepository) {
        this.stationRepository = stationRepository;
        this.equipementRepository = equipementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StationResponse> lister() {
        return stationRepository.findAllByOrderByNomAsc().stream()
                .map(StationResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StationResponse trouver(Long id) {
        return stationRepository.findById(id)
                .map(StationResponse::from)
                .orElseThrow(() -> new StationIntrouvableException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ReseauStationsResponse consulterReseau() {
        List<Equipement> equipements = equipementRepository.findAllByOrderByReferenceUniqueAsc();
        Map<Long, List<EquipementResponse>> equipementsParStation = equipements.stream()
                .collect(Collectors.groupingBy(equipement -> equipement.getStation().getId(),
                        Collectors.mapping(EquipementResponse::from, Collectors.toList())));

        return new ReseauStationsResponse(lister(), equipementsParStation, equipements.size(),
                compter(equipements, EquipementEtat.EN_PANNE),
                compter(equipements, EquipementEtat.EN_MAINTENANCE));
    }

    private static long compter(List<Equipement> equipements, EquipementEtat etat) {
        return equipements.stream().filter(equipement -> equipement.getEtatFonctionnement() == etat).count();
    }
}
