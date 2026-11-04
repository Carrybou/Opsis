package altn72.opsis.service.tableaudebord;

import altn72.opsis.dto.tableaudebord.FiltresTableauDeBord;
import altn72.opsis.dto.tableaudebord.IncidentActifResponse;
import altn72.opsis.dto.tableaudebord.TableauDeBordResponse;
import altn72.opsis.dto.tableaudebord.TableauDeBordResponse.GroupeIncidents;
import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.equipement.EquipementEtat;
import altn72.opsis.model.equipement.EquipementRepository;
import altn72.opsis.model.equipement.EquipementType;
import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentRepository;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.incident.NiveauAttention;
import altn72.opsis.model.station.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableauDeBordServiceTest {

    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 11, 4, 12, 0);

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private EquipementRepository equipementRepository;

    private TableauDeBordService tableauDeBordService;

    @BeforeEach
    void donnees() {
        ZoneId zone = ZoneId.systemDefault();
        tableauDeBordService = new TableauDeBordService(incidentRepository, equipementRepository,
                Clock.fixed(MAINTENANT.atZone(zone).toInstant(), zone));

        Station chatelet = station(1L, "Châtelet");
        Station gareDeLyon = station(2L, "Gare de Lyon");
        when(incidentRepository.findAll()).thenReturn(List.of(
                incident(1L, IncidentStatut.EN_COURS, 100, null, equipement(10L, chatelet, EquipementType.ASCENSEUR)),
                incident(2L, IncidentStatut.OUVERT, 80, null, equipement(20L, gareDeLyon, EquipementType.ESCALATOR)),
                incident(3L, IncidentStatut.OUVERT, 50, null, equipement(30L, chatelet, EquipementType.ECRAN)),
                incident(4L, IncidentStatut.OUVERT, 44, null, equipement(40L, chatelet, EquipementType.ASCENSEUR)),
                // Résolu (RG6) : absent de la liste et des niveaux, compté seulement dans « résolus en 7 jours ».
                incident(5L, IncidentStatut.RESOLU, 100, MAINTENANT.minusDays(2), equipement(50L, gareDeLyon, EquipementType.VALIDEUR))));
        when(equipementRepository.findAll()).thenReturn(List.of(equipement(10L, chatelet, EquipementType.ASCENSEUR)));
    }

    @Test
    void consulter_groupeParUrgenceAvecLEcheanceDuSeuilSuivant() {
        TableauDeBordResponse tableau = tableauDeBordService.consulter(FiltresTableauDeBord.aucun());

        assertThat(tableau.groupes()).extracting(GroupeIncidents::niveau).containsExactly(
                NiveauAttention.ROUGE, NiveauAttention.ORANGE, NiveauAttention.VERT, NiveauAttention.AFFECTE);
        IncidentActifResponse vert = tableau.groupes().get(2).incidents().get(0);
        assertThat(vert.echeance()).isEqualTo("orange dans 4 h 0 min");
        assertThat(vert.progression()).isEqualTo(91);
        assertThat(tableau.groupes().get(0).incidents().get(0).echeance()).isEqualTo("en retard de 8 h 0 min");

        assertThat(tableau.indicateurs().actifs()).isEqualTo(4);
        assertThat(tableau.indicateurs().sansPrestataire()).isEqualTo(3);
        assertThat(tableau.indicateurs().resolus7j()).isEqualTo(1);
    }

    @Test
    void consulter_filtres_sAppliquentATouteLaPageSaufAuxCompteursDesPuces() {
        TableauDeBordResponse tableau = tableauDeBordService.consulter(
                new FiltresTableauDeBord(1L, Set.of(NiveauAttention.VERT), null, "ascen", null));

        assertThat(tableau.nombreAffiches()).isEqualTo(1);
        assertThat(tableau.indicateurs().actifs()).isEqualTo(1);
        // Les puces gardent le compte de chaque niveau dans le périmètre (station + recherche), pour pouvoir en cocher une autre.
        assertThat(tableau.compteParNiveau().get(NiveauAttention.AFFECTE)).isEqualTo(1);
        assertThat(tableau.compteParNiveau().get(NiveauAttention.ORANGE)).isZero();
    }

    private static Station station(Long id, String nom) {
        Station station = new Station();
        station.setId(id);
        station.setNom(nom);
        return station;
    }

    private static Equipement equipement(Long id, Station station, EquipementType type) {
        Equipement equipement = new Equipement();
        equipement.setId(id);
        equipement.setReferenceUnique("EQ-" + id);
        equipement.setType(type);
        equipement.setEtatFonctionnement(EquipementEtat.EN_PANNE);
        equipement.setIdStation(station);
        return equipement;
    }

    private static Incident incident(Long id, IncidentStatut statut, int heuresDepuisOuverture,
                                     LocalDateTime resolution, Equipement equipement) {
        Incident incident = new Incident();
        incident.setId(id);
        incident.setDescription("Incident " + id + " " + equipement.getType());
        incident.setStatut(statut);
        incident.setDateHeureOuverture(MAINTENANT.minusHours(heuresDepuisOuverture));
        incident.setDateHeureResolution(resolution);
        incident.setIdEquipement(equipement);
        return incident;
    }
}
