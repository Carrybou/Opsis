package altn72.opsis.service.station;

import altn72.opsis.dto.station.ReseauStationsResponse;
import altn72.opsis.exception.StationIntrouvableException;
import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.equipement.EquipementEtat;
import altn72.opsis.model.equipement.EquipementRepository;
import altn72.opsis.model.station.Station;
import altn72.opsis.model.station.StationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    @Mock
    private StationRepository stationRepository;

    @Mock
    private EquipementRepository equipementRepository;

    @InjectMocks
    private StationService stationService;

    @Test
    void consulterReseau_regroupeLesEquipementsParStationEtCalculeLaSynthese() {
        Station chatelet = station(1L, "Châtelet");
        Station gareDeLyon = station(2L, "Gare de Lyon");
        when(stationRepository.findAllByOrderByNomAsc()).thenReturn(List.of(chatelet, gareDeLyon));
        when(equipementRepository.findAllByOrderByReferenceUniqueAsc()).thenReturn(List.of(
                equipement(2L, "ASC-CHT-001", EquipementEtat.EN_PANNE, chatelet),
                equipement(3L, "ECR-GDL-001", EquipementEtat.EN_MAINTENANCE, gareDeLyon),
                equipement(1L, "ESC-CHT-001", EquipementEtat.FONCTIONNEL, chatelet)));

        ReseauStationsResponse reseau = stationService.consulterReseau();

        assertThat(reseau.stations()).hasSize(2);
        assertThat(reseau.nombreEquipements()).isEqualTo(3);
        assertThat(reseau.nombreEnPanne()).isEqualTo(1);
        assertThat(reseau.nombreEnMaintenance()).isEqualTo(1);
        assertThat(reseau.equipementsParStation().get(1L)).extracting("referenceUnique").containsExactly("ASC-CHT-001", "ESC-CHT-001");
        assertThat(reseau.equipementsParStation().get(2L)).extracting("referenceUnique").containsExactly("ECR-GDL-001");
    }

    @Test
    void trouver_stationInexistante_leveUneExceptionMetier() {
        when(stationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stationService.trouver(99L))
                .isInstanceOf(StationIntrouvableException.class)
                .hasMessage("La station n°99 est introuvable");
    }

    private static Station station(Long id, String nom) {
        Station station = new Station();
        station.setId(id);
        station.setNom(nom);
        return station;
    }

    private static Equipement equipement(Long id, String reference, EquipementEtat etat, Station station) {
        Equipement equipement = new Equipement();
        equipement.setId(id);
        equipement.setReferenceUnique(reference);
        equipement.setEtatFonctionnement(etat);
        equipement.setIdStation(station);
        return equipement;
    }
}
