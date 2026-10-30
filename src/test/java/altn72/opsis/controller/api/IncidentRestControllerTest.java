package altn72.opsis.controller.api;

import altn72.opsis.dto.incident.AffectationRequest;
import altn72.opsis.dto.incident.IncidentDetailResponse;
import altn72.opsis.dto.incident.IncidentModificationRequest;
import altn72.opsis.dto.incident.IncidentResponse;
import altn72.opsis.exception.AffectationIncidentImpossibleException;
import altn72.opsis.exception.IncidentIntrouvableException;
import altn72.opsis.exception.ModificationIncidentInterditeException;
import altn72.opsis.exception.PrestataireIntrouvableException;
import altn72.opsis.model.equipement.EquipementType;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.JwtService;
import altn72.opsis.security.UtilisateurConnecte;
import altn72.opsis.service.incident.IncidentInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void consulter_technicien_renvoieLeDetail() throws Exception {
        when(incidentService.consulterDetail(1L)).thenReturn(detail());

        mockMvc.perform(get("/api/incidents/1").header(HttpHeaders.AUTHORIZATION, "Bearer token-technicien"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identifiant").value(1))
                .andExpect(jsonPath("$.referenceEquipement").value("ASC-CHT-001"));
    }

    @Test
    void consulter_incidentInexistant_renvoie404() throws Exception {
        when(incidentService.consulterDetail(99L)).thenThrow(new IncidentIntrouvableException(99L));

        mockMvc.perform(get("/api/incidents/99").header(HttpHeaders.AUTHORIZATION, "Bearer token-agent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("L'incident n°99 est introuvable"));
    }

    @Test
    void declarer_responsable_renvoie403() throws Exception {
        mockMvc.perform(post("/api/incidents")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-responsable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DECLARATION_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(incidentService);
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
    void modifier_responsable_renvoieLIncidentModifie() throws Exception {
        when(incidentService.modifier(eq(1L), any(IncidentModificationRequest.class), eq(RESPONSABLE))).thenReturn(incident());

        mockMvc.perform(patch("/api/incidents/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-responsable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DESCRIPTION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Ascenseur bloqué entre deux niveaux"));
    }

    @Test
    void modifier_agentSurIncidentAffecte_renvoie403AvecLeMotif() throws Exception {
        when(incidentService.modifier(eq(1L), any(IncidentModificationRequest.class), eq(AGENT)))
                .thenThrow(new ModificationIncidentInterditeException(
                        "L'incident n°1 est déjà affecté à un prestataire : seul un responsable d'exploitation peut le modifier"));

        mockMvc.perform(patch("/api/incidents/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-agent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DESCRIPTION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(
                        "L'incident n°1 est déjà affecté à un prestataire : seul un responsable d'exploitation peut le modifier"));
    }

    @Test
    void modifier_descriptionTropCourte_renvoie400() throws Exception {
        mockMvc.perform(patch("/api/incidents/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-responsable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"court\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreurs.description").value("La description doit contenir entre 10 et 2000 caractères"));

        verifyNoInteractions(incidentService);
    }

    @Test
    void affecter_agentOuTechnicien_renvoie403() throws Exception {
        for (String token : new String[]{"Bearer token-agent", "Bearer token-technicien"}) {
            mockMvc.perform(put("/api/incidents/1/prestataire")
                            .header(HttpHeaders.AUTHORIZATION, token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"idPrestataire\":2}"))
                    .andExpect(status().isForbidden());
        }

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
    void affecter_prestataireInexistant_renvoie404() throws Exception {
        when(incidentService.affecter(eq(1L), any(AffectationRequest.class), eq(RESPONSABLE)))
                .thenThrow(new PrestataireIntrouvableException(99L));

        mockMvc.perform(put("/api/incidents/1/prestataire")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-responsable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idPrestataire\":99}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Le prestataire n°99 est introuvable"));
    }

    @Test
    void affecter_sansPrestataire_renvoie400() throws Exception {
        mockMvc.perform(put("/api/incidents/1/prestataire")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-responsable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreurs.idPrestataire").value("Veuillez choisir un prestataire"));

        verifyNoInteractions(incidentService);
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
                3L, "Châtelet", null, null, null, "Hugo", null);
    }
}
