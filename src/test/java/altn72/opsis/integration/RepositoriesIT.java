package altn72.opsis.integration;

import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.equipement.EquipementEtat;
import altn72.opsis.model.equipement.EquipementRepository;
import altn72.opsis.model.equipement.EquipementType;
import altn72.opsis.model.evaluation.Evaluation;
import altn72.opsis.model.evaluation.EvaluationRepository;
import altn72.opsis.model.evaluation.NotePrestataire;
import altn72.opsis.model.incident.DeclarationsUtilisateur;
import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentRepository;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.station.Station;
import altn72.opsis.model.station.StationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Ce que les tests avec repositories simulés ne peuvent pas prouver : les requêtes elles-mêmes et les contraintes du schéma.
// Chaque test est annulé à la fin (@DataJpaTest est transactionnel) et s'appuie sur les données de démonstration de bdd.sql.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepositoriesIT extends AvecBaseMysql {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private EquipementRepository equipementRepository;

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private StationRepository stationRepository;

    // RG7
    @Test
    void historique_estTrieParDateDeResolutionDuPlusRecentAuPlusAncien() {
        List<Incident> historique = incidentRepository.findByStatutOrderByDateHeureResolutionDesc(IncidentStatut.RESOLU);

        assertThat(historique).isNotEmpty().allMatch(Incident::estResolu);
        assertThat(historique).extracting(Incident::getDateHeureResolution)
                .isSortedAccordingTo(Comparator.<LocalDateTime>reverseOrder());
    }

    // RG7 : l'équipement 1 a deux incidents dans les données de démonstration.
    @Test
    void incidentsDUnEquipement_sontTriesParDateDOuverture() {
        List<Incident> incidents = incidentRepository.findByEquipement_IdOrderByDateHeureOuvertureDesc(1L);

        assertThat(incidents).hasSizeGreaterThanOrEqualTo(2);
        assertThat(incidents).extracting(Incident::getDateHeureOuverture)
                .isSortedAccordingTo(Comparator.<LocalDateTime>reverseOrder());
    }

    // RG6
    @Test
    void incidentsActifs_nExcluentQueLesResolus() {
        List<Incident> actifs = incidentRepository.findByStatutNotOrderByDateHeureOuvertureDesc(IncidentStatut.RESOLU);

        assertThat(actifs).isNotEmpty().noneMatch(Incident::estResolu);
    }

    // État d'un équipement à la résolution : l'incident résolu lui-même ne doit pas compter.
    @Test
    void autresIncidentsDUnEquipement_exclutLIncidentDonne() {
        assertThat(incidentRepository.existsByEquipement_IdAndStatutAndIdNot(2L, IncidentStatut.EN_COURS, 1L)).isFalse();
        assertThat(incidentRepository.existsByEquipement_IdAndStatutAndIdNot(2L, IncidentStatut.EN_COURS, 99L)).isTrue();
        assertThat(incidentRepository.existsByEquipement_IdAndStatutNotAndIdNot(2L, IncidentStatut.RESOLU, 1L)).isFalse();
    }

    // Requête SQL native : prestataires sans évaluation de la démonstration exclus, moyenne calculée par la base.
    @Test
    void notesParPrestataire_calculeMoyenneEtNombreDEvaluations() {
        Map<Long, NotePrestataire> notes = evaluationRepository.calculerNotesParPrestataire().stream()
                .collect(Collectors.toMap(NotePrestataire::getIdPrestataire, Function.identity()));

        assertThat(notes.get(2L).getMoyenne().doubleValue()).isEqualTo(4.5);
        assertThat(notes.get(2L).getNombre().longValue()).isEqualTo(2);
        assertThat(notes.get(1L).getMoyenne().doubleValue()).isEqualTo(3.0);
        assertThat(notes.get(1L).getNombre().longValue()).isEqualTo(1);
    }

    @Test
    void declarationsParUtilisateur_compteLesDeclaresEtLesActifs() {
        Map<Long, DeclarationsUtilisateur> declarations = incidentRepository.compterDeclarationsParUtilisateur().stream()
                .collect(Collectors.toMap(DeclarationsUtilisateur::getIdUtilisateur, Function.identity()));

        DeclarationsUtilisateur agent = declarations.get(4L);
        assertThat(agent.getDeclares().longValue()).isGreaterThanOrEqualTo(agent.getActifs().longValue());
        assertThat(agent.getActifs().longValue()).isPositive();
        assertThat(agent.getDerniereDeclaration()).isNotNull();
    }

    @Test
    void stationsEtEquipements_sontRenvoyesDansLOrdreDAffichage() {
        assertThat(stationRepository.findAllByOrderByNomAsc()).extracting(Station::getNom).isSorted();
        assertThat(equipementRepository.findAllByOrderByReferenceUniqueAsc()).extracting(Equipement::getReferenceUnique).isSorted();
    }

    // RG9
    @Test
    void referenceDEquipement_estUnique() {
        Equipement doublon = new Equipement();
        doublon.setReferenceUnique("ESC-CHT-001");
        doublon.setLibelleDescription("Doublon de référence");
        doublon.setType(EquipementType.ESCALATOR);
        doublon.setEtatFonctionnement(EquipementEtat.FONCTIONNEL);
        doublon.setStation(stationRepository.findById(1L).orElseThrow());

        assertThatThrownBy(() -> equipementRepository.saveAndFlush(doublon))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // RG8 : l'incident 6 est déjà évalué dans les données de démonstration.
    @Test
    void uneSeuleEvaluationParIncident() {
        assertThatThrownBy(() -> evaluationRepository.saveAndFlush(evaluation(6L, 4)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // RG8 : même si le service était contourné, la base refuse une note hors de 1 à 5. L'incident 11 n'est pas évalué.
    @Test
    void noteHorsDeUnACinq_estRefuseeParLaBase() {
        assertThatThrownBy(() -> evaluationRepository.saveAndFlush(evaluation(11L, 6)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Evaluation evaluation(Long idIncident, int note) {
        Evaluation evaluation = new Evaluation();
        evaluation.setNote(note);
        evaluation.setDate(LocalDate.now());
        evaluation.setIncident(incidentRepository.findById(idIncident).orElseThrow());
        return evaluation;
    }
}
