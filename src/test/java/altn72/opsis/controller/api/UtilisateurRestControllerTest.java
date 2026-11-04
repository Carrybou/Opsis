package altn72.opsis.controller.api;

import altn72.opsis.dto.utilisateur.UtilisateurResponse;
import altn72.opsis.exception.IdentifiantDejaUtiliseException;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.JwtService;
import altn72.opsis.security.UtilisateurConnecte;
import altn72.opsis.service.utilisateur.UtilisateurInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UtilisateurRestController.class)
class UtilisateurRestControllerTest {

    private static final String COMPTE_JSON =
            "{\"identifiantConnexion\":\"nouveau.compte\",\"motDePasse\":\"motdepasse\",\"prenom\":\"Alex\",\"profil\":\"ADMIN\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UtilisateurInterface utilisateurService;

    @MockitoBean
    private JwtService jwtService;

    @BeforeEach
    void tokens() {
        when(jwtService.extraireUtilisateur(anyString())).thenReturn(Optional.empty());
        when(jwtService.extraireUtilisateur("token-responsable"))
                .thenReturn(Optional.of(new UtilisateurConnecte(1L, "admin", "Roméo", UtilisateurType.ADMIN)));
        when(jwtService.extraireUtilisateur("token-agent"))
                .thenReturn(Optional.of(new UtilisateurConnecte(4L, "agent.bernard", "Hugo", UtilisateurType.AGENT)));
        when(jwtService.extraireUtilisateur("token-technicien"))
                .thenReturn(Optional.of(new UtilisateurConnecte(2L, "tech.martin", "Lucas", UtilisateurType.TECHNICIEN)));
    }

    // RG11 : sans ce contrôle, n'importe quel profil connecté pourrait se créer un compte responsable.
    @Test
    void creer_agentOuTechnicien_renvoie403() throws Exception {
        for (String token : new String[]{"token-agent", "token-technicien"}) {
            mockMvc.perform(post("/api/utilisateurs")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(COMPTE_JSON))
                    .andExpect(status().isForbidden());
        }

        verifyNoInteractions(utilisateurService);
    }

    @Test
    void creer_responsable_renvoie201() throws Exception {
        when(utilisateurService.creer(any()))
                .thenReturn(new UtilisateurResponse(10L, "nouveau.compte", "Alex", UtilisateurType.ADMIN));

        mockMvc.perform(post("/api/utilisateurs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-responsable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COMPTE_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.identifiant").value(10));
    }

    @Test
    void creer_identifiantDejaUtilise_renvoie409AuFormatCommun() throws Exception {
        when(utilisateurService.creer(any())).thenThrow(new IdentifiantDejaUtiliseException("nouveau.compte"));

        mockMvc.perform(post("/api/utilisateurs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-responsable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COMPTE_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void creer_motDePasseTropCourt_renvoie400SansRienCreer() throws Exception {
        mockMvc.perform(post("/api/utilisateurs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-responsable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifiantConnexion\":\"nouveau.compte\",\"motDePasse\":\"1\",\"prenom\":\"Alex\",\"profil\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(utilisateurService);
    }
}
