package altn72.opsis.service.equipement;

import altn72.opsis.dto.FicheEquipementResponse;
import altn72.opsis.dto.IncidentResume;
import altn72.opsis.exception.EquipementIntrouvableException;
import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.equipement.EquipementRepository;
import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentRepository;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.station.Station;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipementServiceTest {

    private static final Long ID_EQUIPEMENT = 1L;

    @Mock
    private EquipementRepository equipementRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @InjectMocks
    private EquipementService equipementService;

    @Test
    void consulterFiche_separeIncidentsEnCoursEtPassesEnConservantLOrdre() {
        LocalDateTime maintenant = LocalDateTime.now();
        Incident enCoursRecent = incident(10L, IncidentStatut.OUVERT, maintenant.minus(1, ChronoUnit.HOURS));
        Incident resoluRecent = incident(11L, IncidentStatut.RESOLU, maintenant.minus(2, ChronoUnit.DAYS));
        Incident enCoursAncien = incident(12L, IncidentStatut.EN_COURS, maintenant.minus(3, ChronoUnit.DAYS));
        Incident resoluAncien = incident(13L, IncidentStatut.RESOLU, maintenant.minus(10, ChronoUnit.DAYS));

        when(equipementRepository.findById(ID_EQUIPEMENT)).thenReturn(Optional.of(equipement()));
        when(incidentRepository.findByIdEquipement_IdOrderByDateHeureOuvertureDesc(ID_EQUIPEMENT))
                .thenReturn(List.of(enCoursRecent, resoluRecent, enCoursAncien, resoluAncien));

        FicheEquipementResponse fiche = equipementService.consulterFiche(ID_EQUIPEMENT);

        assertThat(fiche.referenceUnique()).isEqualTo("ESC-CHT-001");
        assertThat(fiche.nomStation()).isEqualTo("Châtelet");
        assertThat(fiche.incidentsEnCours()).extracting(IncidentResume::identifiant).containsExactly(10L, 12L);
        assertThat(fiche.incidentsPasses()).extracting(IncidentResume::identifiant).containsExactly(11L, 13L);
    }

    @Test
    void consulterFiche_equipementSansIncident_renvoieDesListesVides() {
        when(equipementRepository.findById(ID_EQUIPEMENT)).thenReturn(Optional.of(equipement()));
        when(incidentRepository.findByIdEquipement_IdOrderByDateHeureOuvertureDesc(ID_EQUIPEMENT)).thenReturn(List.of());

        FicheEquipementResponse fiche = equipementService.consulterFiche(ID_EQUIPEMENT);

        assertThat(fiche.incidentsEnCours()).isEmpty();
        assertThat(fiche.incidentsPasses()).isEmpty();
    }

    @Test
    void consulterFiche_equipementInexistant_leveEquipementIntrouvableException() {
        when(equipementRepository.findById(ID_EQUIPEMENT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> equipementService.consulterFiche(ID_EQUIPEMENT))
                .isInstanceOf(EquipementIntrouvableException.class)
                .hasMessage("L'équipement n°1 est introuvable");
    }

    private static Equipement equipement() {
        Station station = new Station();
        station.setId(1L);
        station.setNom("Châtelet");

        Equipement equipement = new Equipement();
        equipement.setId(ID_EQUIPEMENT);
        equipement.setReferenceUnique("ESC-CHT-001");
        equipement.setIdStation(station);
        return equipement;
    }

    private static Incident incident(Long id, IncidentStatut statut, LocalDateTime ouverture) {
        Incident incident = new Incident();
        incident.setId(id);
        incident.setStatut(statut);
        incident.setDateHeureOuverture(ouverture);
        return incident;
    }
}
