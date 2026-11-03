package altn72.opsis.controller.web;

import altn72.opsis.dto.prestataire.FichePrestataireResponse;
import altn72.opsis.dto.prestataire.InterventionResponse;
import altn72.opsis.dto.prestataire.PrestataireResponse;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.JwtService;
import altn72.opsis.security.UtilisateurConnecte;
import altn72.opsis.service.prestataire.PrestataireInterface;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PrestataireViewController.class)
class PrestataireViewControllerTest {

    private static final UtilisateurConnecte RESPONSABLE =
            new UtilisateurConnecte(1L, "admin", "Roméo", UtilisateurType.ADMIN);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PrestataireInterface prestataireService;

    // Requis par le JwtInterceptor enregistré dans WebConfig.
    @MockitoBean
    private JwtService jwtService;

    @Test
    void afficherFiche_responsable_voitLesInterventionsEtPeutEvaluerCellesNonNotees() throws Exception {
        when(prestataireService.consulterFiche(2L)).thenReturn(fiche());

        mockMvc.perform(get("/prestataires/2").sessionAttr(UtilisateurConnecte.ATTRIBUT, RESPONSABLE))
                .andExpect(status().isOk())
                .andExpect(view().name("prestataires/fiche"))
                .andExpect(content().string(containsString("Mobilis Maintenance")))
                .andExpect(content().string(containsString("★ 4,0")))
                .andExpect(content().string(containsString("Bruit anormal puis arrêt complet")))
                .andExpect(content().string(containsString("★★★★☆")))
                .andExpect(content().string(containsString("/incidents/9?evaluer=true#evaluation")));
    }

    private static FichePrestataireResponse fiche() {
        return new FichePrestataireResponse(
                new PrestataireResponse(2L, "Mobilis Maintenance", "01 45 67 89 10", 4.0, 1),
                List.of(new InterventionResponse(4L, "Bruit anormal puis arrêt complet de l'escalator", IncidentStatut.EN_COURS,
                        LocalDateTime.of(2026, 10, 11, 9, 0), null, 4L, "ESC-NAT-001", "Nation", null)),
                List.of(new InterventionResponse(9L, "Arrêt d'urgence déclenché", IncidentStatut.RESOLU,
                                LocalDateTime.of(2026, 9, 20, 8, 5), LocalDateTime.of(2026, 9, 20, 9, 30), 9L, "ESC-LDF-001", "La Défense", null),
                        new InterventionResponse(6L, "Marche cassée en bas de l'escalator", IncidentStatut.RESOLU,
                                LocalDateTime.of(2026, 9, 2, 7, 30), LocalDateTime.of(2026, 9, 3, 14, 0), 1L, "ESC-CHT-001", "Châtelet", 4)));
    }
}
