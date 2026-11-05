package altn72.opsis.controller.api;

import altn72.opsis.dto.evaluation.EvaluationRequest;
import altn72.opsis.dto.evaluation.EvaluationResponse;
import altn72.opsis.dto.incident.AffectationRequest;
import altn72.opsis.dto.incident.IncidentDetailResponse;
import altn72.opsis.dto.incident.IncidentResponse;
import altn72.opsis.dto.tableaudebord.IncidentActifResponse;
import altn72.opsis.exception.conflit.AffectationIncidentImpossibleException;
import altn72.opsis.exception.introuvable.IncidentIntrouvableException;
import altn72.opsis.model.equipement.EquipementType;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.incident.NiveauAttention;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.JwtService;
import altn72.opsis.security.UtilisateurConnecte;
import altn72.opsis.service.evaluation.EvaluationInterface;
import altn72.opsis.service.incident.IncidentInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IncidentRestController.class)
class IncidentRestControllerTest {

    private static final UtilisateurConnecte RESPONSABLE =
            new UtilisateurConnecte(1L, "admin", "Roméo", UtilisateurType.ADMIN);
    private static final UtilisateurConnecte AGENT =
            new UtilisateurConnecte(4L, "agent.bernard", "Hugo", UtilisateurType.AGENT);
    private static final UtilisateurConnecte TECHNICIEN =
            new UtilisateurConnecte(2L, "tech.martin", "Lucas", UtilisateurType.TECHNICIEN);

    private static final String DESCRIPTION_JSON = "{\"description\":\"Ascenseur bloqué entre deux niveaux\"}";
    private static final String DECLARATION_JSON = "{\"idEquipement\":2,\"description\":\"Ascenseur bloqué entre deux niveaux\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IncidentInterface incidentService;

    @MockitoBean
    private EvaluationInterface evaluationService;

    @MockitoBean
    private JwtService jwtService;

    // Chaque profil a son propre token simulé : le JwtInterceptor en déduit l'utilisateur connecté.
    @BeforeEach
    void tokens() {
        when(jwtService.extraireUtilisateur(anyString())).thenReturn(Optional.empty());
        when(jwtService.extraireUtilisateur("token-responsable")).thenReturn(Optional.of(RESPONSABLE));
        when(jwtService.extraireUtilisateur("token-agent")).thenReturn(Optional.of(AGENT));
        when(jwtService.extraireUtilisateur("token-technicien")).thenReturn(Optional.of(TECHNICIEN));
    }

    @Test
    void consulter_sansToken_renvoie401() throws Exception {
        mockMvc.perform(get("/api/incidents/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(incidentService);
    }

    @Test
    void listerActifs_renvoieLesIncidentsAvecLeurNiveau() throws Exception {
        when(incidentService.listerActifs()).thenReturn(List.of(new IncidentActifResponse(1L, "Ascenseur bloqué entre deux niveaux",
                NiveauAttention.ORANGE, LocalDateTime.of(2026, 10, 10, 8, 15), "2 j 0 h", "rouge dans 1 j 0 h", 0,
                2L, "ASC-CHT-001", EquipementType.ASCENSEUR, 3L, "Châtelet", null, null)));

        mockMvc.perform(get("/api/incidents").header(HttpHeaders.AUTHORIZATION, "Bearer token-technicien"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].identifiant").value(1))
                .andExpect(jsonPath("$[0].niveau").value("ORANGE"));
    }

    @Test
    void consulter_incidentInexistant_renvoie404() throws Exception {
        when(incidentService.consulterDetail(99L)).thenThrow(new IncidentIntrouvableException(99L));

        mockMvc.perform(get("/api/incidents/99").header(HttpHeaders.AUTHORIZATION, "Bearer token-agent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("L'incident n°99 est introuvable"));
    }

    @Test
    void declarer_agent_renvoie201() throws Exception {
        when(incidentService.declarer(any(), eq(AGENT.id()))).thenReturn(incident());

        mockMvc.perform(post("/api/incidents")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-agent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DECLARATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.identifiant").value(1));
    }

    @Test
    void modifier_technicien_renvoie403() throws Exception {
        mockMvc.perform(patch("/api/incidents/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-technicien")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DESCRIPTION_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(incidentService);
    }

    @Test
    void affecter_responsable_renvoieLIncidentAffecte() throws Exception {
        when(incidentService.affecter(eq(1L), any(AffectationRequest.class), eq(RESPONSABLE))).thenReturn(
                new IncidentResponse(1L, "Ascenseur bloqué entre deux niveaux", IncidentStatut.EN_COURS,
                        LocalDateTime.of(2026, 10, 10, 8, 15), null,
                        2L, "ASC-CHT-001", "Ascenseur accès quai ligne 1", "Châtelet",
                        2L, "Mobilis Maintenance", 4L, "Hugo"));

        mockMvc.perform(put("/api/incidents/1/prestataire")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-responsable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idPrestataire\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("EN_COURS"))
                .andExpect(jsonPath("$.nomPrestataire").value("Mobilis Maintenance"));
    }

    @Test
    void affecter_incidentResolu_renvoie409() throws Exception {
        when(incidentService.affecter(eq(6L), any(AffectationRequest.class), eq(RESPONSABLE)))
                .thenThrow(new AffectationIncidentImpossibleException("L'incident n°6 est résolu : il ne peut plus être affecté ni réaffecté"));

        mockMvc.perform(put("/api/incidents/6/prestataire")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-responsable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idPrestataire\":2}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("L'incident n°6 est résolu : il ne peut plus être affecté ni réaffecté"));
    }

    @Test
    void resoudre_responsableOuTechnicien_renvoieLeDetail() throws Exception {
        when(incidentService.resoudre(eq(1L), any())).thenReturn(detail());

        for (String token : new String[]{"token-responsable", "token-technicien"}) {
            mockMvc.perform(post("/api/incidents/1/resolution")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.identifiant").value(1));
        }
    }

    @Test
    void evaluer_agentOuTechnicien_renvoie403() throws Exception {
        for (String token : new String[]{"Bearer token-agent", "Bearer token-technicien"}) {
            mockMvc.perform(post("/api/incidents/11/evaluation")
                            .header(HttpHeaders.AUTHORIZATION, token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"note\":4}"))
                    .andExpect(status().isForbidden());
        }

        verifyNoInteractions(evaluationService);
    }

    @Test
    void evaluer_responsable_renvoie201AvecLEvaluation() throws Exception {
        when(evaluationService.evaluer(eq(11L), any(EvaluationRequest.class), eq(RESPONSABLE)))
                .thenReturn(new EvaluationResponse(6L, 4, "Intervention rapide", LocalDate.of(2026, 11, 3)));

        mockMvc.perform(post("/api/incidents/11/evaluation")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-responsable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":4,\"commentaire\":\"Intervention rapide\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/incidents/11"))
                .andExpect(jsonPath("$.note").value(4))
                .andExpect(jsonPath("$.commentaire").value("Intervention rapide"));
    }

    @Test
    void evaluer_noteHorsBornes_renvoie400() throws Exception {
        mockMvc.perform(post("/api/incidents/11/evaluation")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-responsable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":6}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreurs.note").value("La note doit être comprise entre 1 et 5"));

        verifyNoInteractions(evaluationService);
    }

    // §7.4 : un identifiant mal formé donne un message clair, sans nom de classe Java.
    @Test
    void consulter_identifiantNonNumerique_renvoie400SansDetailTechnique() throws Exception {
        mockMvc.perform(get("/api/incidents/abc").header(HttpHeaders.AUTHORIZATION, "Bearer token-agent"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Le paramètre « id » n'est pas valide"));
    }

    @Test
    void declarer_formatNonAccepte_renvoie415SansDetailTechnique() throws Exception {
        mockMvc.perform(post("/api/incidents")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-agent")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("texte"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message").value("Ce format de données n'est pas accepté"));
    }

    @Test
    void consulter_erreurInattendue_renvoie500SansDetailTechnique() throws Exception {
        when(incidentService.consulterDetail(1L)).thenThrow(new IllegalStateException("détail interne"));

        mockMvc.perform(get("/api/incidents/1").header(HttpHeaders.AUTHORIZATION, "Bearer token-agent"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Une erreur interne est survenue. Réessayez dans quelques instants."));
    }

    // RG8 : une note décimale est refusée, pas tronquée.
    @Test
    void evaluer_noteDecimale_renvoie400SansRienEnregistrer() throws Exception {
        mockMvc.perform(post("/api/incidents/1/evaluation")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-responsable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":4.5}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(evaluationService);
    }

    // RG10 : deux utilisateurs modifient le même incident en même temps.
    @Test
    void resoudre_modificationSimultanee_renvoie409() throws Exception {
        when(incidentService.resoudre(1L, TECHNICIEN))
                .thenThrow(new ObjectOptimisticLockingFailureException("Incident", 1L));

        mockMvc.perform(post("/api/incidents/1/resolution")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-technicien"))
                .andExpect(status().isConflict());
    }

    private static IncidentResponse incident() {
        return new IncidentResponse(1L, "Ascenseur bloqué entre deux niveaux", IncidentStatut.OUVERT,
                LocalDateTime.of(2026, 10, 10, 8, 15), null,
                2L, "ASC-CHT-001", "Ascenseur accès quai ligne 1", "Châtelet",
                null, null, 4L, "Hugo");
    }

    private static IncidentDetailResponse detail() {
        return new IncidentDetailResponse(1L, "Ascenseur bloqué entre deux niveaux", IncidentStatut.OUVERT,
                LocalDateTime.of(2026, 10, 10, 8, 15), null, false, "2 h 0 min",
                2L, "ASC-CHT-001", "Ascenseur accès quai ligne 1", EquipementType.ASCENSEUR,
                3L, "Châtelet", null, null, null, "Hugo", null, NiveauAttention.VERT);
    }
}
