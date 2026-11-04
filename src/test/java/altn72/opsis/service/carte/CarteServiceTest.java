package altn72.opsis.service.carte;

import altn72.opsis.dto.carte.CarteReseauResponse;
import altn72.opsis.dto.carte.CarteReseauResponse.StationCarte;
import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.equipement.EquipementEtat;
import altn72.opsis.model.equipement.EquipementRepository;
import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentRepository;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.station.Station;
import altn72.opsis.model.station.StationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarteServiceTest {

    @Mock
    private StationRepository stationRepository;

    @Mock
    private EquipementRepository equipementRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @InjectMocks
    private CarteService carteService;

    @Test
    void consulter_laSituationEstLEtatLePlusGraveEtLesEquipementsSontTriesParGravite() {
        Station chatelet = station(1L, "Châtelet", "48.858372", "2.347061");
        Station republique = station(2L, "République", "48.867498", "2.363842");
        Equipement fonctionnel = equipement(1L, "ASC-001", EquipementEtat.FONCTIONNEL, chatelet);
        Equipement enMaintenance = equipement(2L, "ESC-001", EquipementEtat.EN_MAINTENANCE, chatelet);
        Equipement enPanne = equipement(3L, "ECR-001", EquipementEtat.EN_PANNE, chatelet);
        Equipement seulEnMaintenance = equipement(4L, "ESC-002", EquipementEtat.EN_MAINTENANCE, republique);
        when(stationRepository.findAllByOrderByNomAsc()).thenReturn(List.of(chatelet, republique));
        when(equipementRepository.findAllByOrderByReferenceUniqueAsc()).thenReturn(List.of(fonctionnel, enMaintenance, enPanne, seulEnMaintenance));

        CarteReseauResponse carte = carteService.consulter();

        StationCarte premiere = carte.stations().getFirst();
        assertThat(premiere.nom()).isEqualTo("Châtelet");
        assertThat(premiere.situation()).isEqualTo(EquipementEtat.EN_PANNE);
        assertThat(premiere.equipements()).extracting("reference").containsExactly("ECR-001", "ESC-001", "ASC-001");
        assertThat(premiere.nombreEnPanne()).isEqualTo(1);
        assertThat(premiere.nombreEnMaintenance()).isEqualTo(1);
        assertThat(premiere.resumeEquipements()).isEqualTo("3 équipements · 1 en panne · 1 en maintenance");
        assertThat(carte.stations().get(1).situation()).isEqualTo(EquipementEtat.EN_MAINTENANCE);
        assertThat(carte.nombreEnPanne()).isEqualTo(1);
        assertThat(carte.nombreEnMaintenance()).isEqualTo(1);
    }

    @Test
    void consulter_stationSansEquipementNiCoordonnees_resteDansLaReponse() {
        when(stationRepository.findAllByOrderByNomAsc()).thenReturn(List.of(station(1L, "Nouvelle station", null, null)));

        CarteReseauResponse carte = carteService.consulter();

        StationCarte station = carte.stations().getFirst();
        assertThat(station.situation()).isNull();
        assertThat(station.estLocalisee()).isFalse();
        assertThat(station.equipements()).isEmpty();
        assertThat(carte.nombreSansEquipement()).isEqualTo(1);
        assertThat(station.resumeEquipements()).isEqualTo("Aucun équipement");
    }

    @Test
    void consulter_rattacheLesIncidentsActifsASonEquipement() {
        Station chatelet = station(1L, "Châtelet", "48.858372", "2.347061");
        Equipement ascenseur = equipement(1L, "ASC-001", EquipementEtat.EN_PANNE, chatelet);
        Equipement escalator = equipement(2L, "ESC-001", EquipementEtat.FONCTIONNEL, chatelet);
        Incident actif = new Incident();
        actif.setId(7L);
        actif.setDescription("Ascenseur bloqué");
        actif.setStatut(IncidentStatut.OUVERT);
        actif.setEquipement(ascenseur);
        when(stationRepository.findAllByOrderByNomAsc()).thenReturn(List.of(chatelet));
        when(equipementRepository.findAllByOrderByReferenceUniqueAsc()).thenReturn(List.of(ascenseur, escalator));
        when(incidentRepository.findByStatutNotOrderByDateHeureOuvertureDesc(IncidentStatut.RESOLU)).thenReturn(List.of(actif));

        StationCarte station = carteService.consulter().stations().getFirst();

        assertThat(station.equipements().getFirst().incidentsActifs()).extracting("identifiant").containsExactly(7L);
        assertThat(station.equipements().get(1).incidentsActifs()).isEmpty();
    }

    private static Station station(Long id, String nom, String latitude, String longitude) {
        Station station = new Station();
        station.setId(id);
        station.setNom(nom);
        station.setLatitude(latitude == null ? null : new BigDecimal(latitude));
        station.setLongitude(longitude == null ? null : new BigDecimal(longitude));
        return station;
    }

    private static Equipement equipement(Long id, String reference, EquipementEtat etat, Station station) {
        Equipement equipement = new Equipement();
        equipement.setId(id);
        equipement.setReferenceUnique(reference);
        equipement.setEtatFonctionnement(etat);
        equipement.setStation(station);
        return equipement;
    }
}
