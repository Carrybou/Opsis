package altn72.opsis.controller.web;

import altn72.opsis.dto.utilisateur.GestionComptesResponse;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.JwtService;
import altn72.opsis.security.UtilisateurConnecte;
import altn72.opsis.service.utilisateur.UtilisateurInterface;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(CompteViewController.class)
class CompteViewControllerTest {

    private static final UtilisateurConnecte RESPONSABLE =
            new UtilisateurConnecte(1L, "admin", "Roméo", UtilisateurType.ADMIN);
    private static final UtilisateurConnecte TECHNICIEN =
            new UtilisateurConnecte(2L, "tech.martin", "Lucas", UtilisateurType.TECHNICIEN);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UtilisateurInterface utilisateurService;

    // Requis par le JwtInterceptor enregistré dans WebConfig.
    @MockitoBean
    private JwtService jwtService;

    @Test
    void lister_horsResponsable_estRefuseParLaRoute() throws Exception {
        mockMvc.perform(get("/comptes").sessionAttr(UtilisateurConnecte.ATTRIBUT, TECHNICIEN))
                .andExpect(status().isForbidden());

        verifyNoInteractions(utilisateurService);
    }

    @Test
    void creer_sansMotDePasse_rouvreLeFormulaireAvecLErreur() throws Exception {
        when(utilisateurService.consulterComptes()).thenReturn(new GestionComptesResponse(List.of(),
                Map.of(UtilisateurType.ADMIN, 0L, UtilisateurType.AGENT, 0L, UtilisateurType.TECHNICIEN, 0L), 0, null));

        mockMvc.perform(post("/comptes")
                        .param("prenom", "Camille").param("identifiantConnexion", "camille.r").param("profil", "AGENT")
                        .param("motDePasse", "")
                        .sessionAttr(UtilisateurConnecte.ATTRIBUT, RESPONSABLE))
                .andExpect(status().isOk())
                .andExpect(view().name("comptes"))
                .andExpect(content().string(containsString("Le mot de passe est obligatoire")))
                .andExpect(content().string(containsString("camille.r")));

        verify(utilisateurService, never()).creer(any());
    }
}
