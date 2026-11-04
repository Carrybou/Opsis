package altn72.opsis.service.equipement;

import altn72.opsis.dto.equipement.EquipementResponse;
import altn72.opsis.dto.equipement.FicheEquipementResponse;
import altn72.opsis.dto.incident.IncidentResume;
import altn72.opsis.exception.introuvable.EquipementIntrouvableException;
import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.equipement.EquipementRepository;
import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EquipementService implements EquipementInterface {

    private final EquipementRepository equipementRepository;
    private final IncidentRepository incidentRepository;
    private final Clock horloge;

    public EquipementService(EquipementRepository equipementRepository, IncidentRepository incidentRepository, Clock horloge) {
        this.equipementRepository = equipementRepository;
        this.incidentRepository = incidentRepository;
        this.horloge = horloge;
    }

    @Override
    public FicheEquipementResponse consulterFiche(Long idEquipement) {
        Equipement equipement = equipementRepository.findById(idEquipement)
                .orElseThrow(() -> new EquipementIntrouvableException(idEquipement));

        LocalDateTime maintenant = LocalDateTime.now(horloge);

        // RG7 : le repository trie par date d'ouverture, partitioningBy conserve cet ordre dans chaque liste.
        Map<Boolean, List<IncidentResume>> incidentsParResolution = incidentRepository
                .findByEquipement_IdOrderByDateHeureOuvertureDesc(idEquipement).stream()
                .collect(Collectors.partitioningBy(Incident::estResolu,
                        Collectors.mapping(incident -> IncidentResume.from(incident, maintenant), Collectors.toList())));

        return FicheEquipementResponse.from(equipement,
                incidentsParResolution.get(false),
                incidentsParResolution.get(true));
    }

    @Override
    @Transactional(readOnly = true)
    public EquipementResponse trouver(Long id) {
        return equipementRepository.findById(id)
                .map(EquipementResponse::from)
                .orElseThrow(() -> new EquipementIntrouvableException(id));
    }
}
