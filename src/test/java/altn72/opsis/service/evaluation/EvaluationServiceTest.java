package altn72.opsis.service.evaluation;

import altn72.opsis.dto.evaluation.EvaluationRequest;
import altn72.opsis.dto.evaluation.EvaluationResponse;
import altn72.opsis.exception.EvaluationImpossibleException;
import altn72.opsis.model.evaluation.Evaluation;
import altn72.opsis.model.evaluation.EvaluationRepository;
import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentRepository;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.prestataire.Prestataire;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.UtilisateurConnecte;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    private static final UtilisateurConnecte RESPONSABLE =
            new UtilisateurConnecte(1L, "admin", "Roméo", UtilisateurType.ADMIN);

    @Mock
    private EvaluationRepository evaluationRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @InjectMocks
    private EvaluationService evaluationService;

    @Test
    void evaluer_incidentResoluEtAffecte_enregistreLEvaluationDuJour() {
        Incident incident = incident(11L, IncidentStatut.RESOLU, true);
        when(incidentRepository.findById(11L)).thenReturn(Optional.of(incident));
        when(evaluationRepository.existsByIdIncident_Id(11L)).thenReturn(false);
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EvaluationResponse reponse = evaluationService.evaluer(11L,
                new EvaluationRequest(4, "  Intervention rapide  "), RESPONSABLE);

        ArgumentCaptor<Evaluation> captee = ArgumentCaptor.forClass(Evaluation.class);
        verify(evaluationRepository).save(captee.capture());
        assertThat(captee.getValue().getIdIncident()).isSameAs(incident);
        assertThat(captee.getValue().getDate()).isEqualTo(LocalDate.now());
        assertThat(reponse.note()).isEqualTo(4);
        assertThat(reponse.commentaire()).isEqualTo("Intervention rapide");
    }

    @Test
    void evaluer_incidentNonResolu_refuseSansRienEnregistrer() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(1L, IncidentStatut.EN_COURS, true)));

        assertThatThrownBy(() -> evaluationService.evaluer(1L, new EvaluationRequest(4, null), RESPONSABLE))
                .isInstanceOf(EvaluationImpossibleException.class)
                .hasMessage("L'incident n°1 n'est pas encore résolu : il ne peut pas être évalué");
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    void evaluer_incidentResoluSansPrestataire_refuseSansRienEnregistrer() {
        when(incidentRepository.findById(14L)).thenReturn(Optional.of(incident(14L, IncidentStatut.RESOLU, false)));

        assertThatThrownBy(() -> evaluationService.evaluer(14L, new EvaluationRequest(4, null), RESPONSABLE))
                .isInstanceOf(EvaluationImpossibleException.class)
                .hasMessage("L'incident n°14 n'a été affecté à aucun prestataire : il n'y a pas d'intervention à évaluer");
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    void evaluer_incidentDejaEvalue_refuseSansRienEnregistrer() {
        when(incidentRepository.findById(6L)).thenReturn(Optional.of(incident(6L, IncidentStatut.RESOLU, true)));
        when(evaluationRepository.existsByIdIncident_Id(6L)).thenReturn(true);

        assertThatThrownBy(() -> evaluationService.evaluer(6L, new EvaluationRequest(2, null), RESPONSABLE))
                .isInstanceOf(EvaluationImpossibleException.class)
                .hasMessage("L'incident n°6 a déjà été évalué");
        verify(evaluationRepository, never()).save(any());
    }

    private static Incident incident(Long id, IncidentStatut statut, boolean affecte) {
        Incident incident = new Incident();
        incident.setId(id);
        incident.setStatut(statut);
        incident.setDateHeureOuverture(LocalDateTime.now().minusDays(2));
        if (affecte) {
            Prestataire prestataire = new Prestataire();
            prestataire.setId(2L);
            prestataire.setNom("Mobilis Maintenance");
            incident.setIdPrestataire(prestataire);
        }
        return incident;
    }
}
