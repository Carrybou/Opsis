package altn72.opsis.service.prestataire;

import altn72.opsis.dto.prestataire.FichePrestataireResponse;
import altn72.opsis.dto.prestataire.InterventionResponse;
import altn72.opsis.dto.prestataire.PrestataireResponse;
import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.evaluation.Evaluation;
import altn72.opsis.model.evaluation.EvaluationRepository;
import altn72.opsis.model.evaluation.NotePrestataire;
import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentRepository;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.prestataire.Prestataire;
import altn72.opsis.model.prestataire.PrestataireRepository;
import altn72.opsis.model.station.Station;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrestataireServiceTest {

    @Mock
    private PrestataireRepository prestataireRepository;

    @Mock
    private EvaluationRepository evaluationRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @InjectMocks
    private PrestataireService prestataireService;

    @Test
    void lister_associeLaNoteMoyenneEtLeNombreDEvaluations() {
        when(prestataireRepository.findAllByOrderByNomAsc()).thenReturn(List.of(
                prestataire(1L, "AscenTech Services"), prestataire(2L, "Mobilis Maintenance")));
        // MySQL renvoie AVG en DECIMAL et COUNT en BIGINT : la projection les expose en Number.
        when(evaluationRepository.calculerNotesParPrestataire()).thenReturn(List.of(note(2L, new BigDecimal("4.5000"), 2L)));

        List<PrestataireResponse> prestataires = prestataireService.lister();

        assertThat(prestataires).extracting(PrestataireResponse::nom)
                .containsExactly("AscenTech Services", "Mobilis Maintenance");
        assertThat(prestataires.get(0).estEvalue()).isFalse();
        assertThat(prestataires.get(0).noteMoyenne()).isNull();
        assertThat(prestataires.get(1).noteMoyenne()).isEqualTo(4.5);
        assertThat(prestataires.get(1).nombreEvaluations()).isEqualTo(2);
    }

    @Test
    void consulterFiche_separeLesInterventionsEnCoursEtTermineesAvecLeurNote() {
        when(prestataireRepository.findById(2L)).thenReturn(Optional.of(prestataire(2L, "Mobilis Maintenance")));
        when(evaluationRepository.calculerNotesParPrestataire()).thenReturn(List.of(note(2L, new BigDecimal("4.0000"), 1L)));
        Incident enCours = incident(4L, IncidentStatut.EN_COURS, LocalDateTime.of(2026, 10, 11, 9, 0), null);
        Incident resoluAncien = incident(6L, IncidentStatut.RESOLU, LocalDateTime.of(2026, 9, 2, 7, 30), LocalDateTime.of(2026, 9, 3, 14, 0));
        Incident resoluRecent = incident(9L, IncidentStatut.RESOLU, LocalDateTime.of(2026, 9, 1, 8, 0), LocalDateTime.of(2026, 9, 20, 9, 30));
        when(incidentRepository.findByPrestataire_IdOrderByDateHeureOuvertureDesc(2L))
                .thenReturn(List.of(enCours, resoluAncien, resoluRecent));
        Evaluation evaluation = new Evaluation();
        evaluation.setNote(4);
        evaluation.setIncident(resoluAncien);
        when(evaluationRepository.findByIncident_Prestataire_Id(2L)).thenReturn(List.of(evaluation));

        FichePrestataireResponse fiche = prestataireService.consulterFiche(2L);

        assertThat(fiche.prestataire().noteMoyenne()).isEqualTo(4.0);
        assertThat(fiche.interventionsEnCours()).extracting(InterventionResponse::identifiant).containsExactly(4L);
        // Terminées : de la résolution la plus récente à la plus ancienne (RG7).
        assertThat(fiche.interventionsTerminees()).extracting(InterventionResponse::identifiant).containsExactly(9L, 6L);
        assertThat(fiche.interventionsTerminees()).extracting(InterventionResponse::note).containsExactly(null, 4);
    }

    private static Incident incident(Long id, IncidentStatut statut, LocalDateTime ouverture, LocalDateTime resolution) {
        Station station = new Station();
        station.setNom("Châtelet");
        Equipement equipement = new Equipement();
        equipement.setId(10L);
        equipement.setReferenceUnique("ESC-CHT-001");
        equipement.setStation(station);
        Incident incident = new Incident();
        incident.setId(id);
        incident.setDescription("Escalator à l'arrêt");
        incident.setStatut(statut);
        incident.setDateHeureOuverture(ouverture);
        incident.setDateHeureResolution(resolution);
        incident.setEquipement(equipement);
        return incident;
    }

    private static Prestataire prestataire(Long id, String nom) {
        Prestataire prestataire = new Prestataire();
        prestataire.setId(id);
        prestataire.setNom(nom);
        prestataire.setContact("contact@exemple.fr");
        return prestataire;
    }

    private static NotePrestataire note(Long idPrestataire, Number moyenne, Number nombre) {
        return new NotePrestataire() {
            public Long getIdPrestataire() { return idPrestataire; }
            public Number getMoyenne() { return moyenne; }
            public Number getNombre() { return nombre; }
        };
    }
}
