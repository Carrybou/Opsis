package altn72.opsis.controller.web;

import altn72.opsis.exception.IdentifiantsInvalidesException;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.JwtService;
import altn72.opsis.security.UtilisateurConnecte;
import altn72.opsis.service.auth.AuthInterface;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(LoginViewController.class)
class LoginViewControllerTest {

    private static final UtilisateurConnecte THOMAS =
            new UtilisateurConnecte(7L, "thomas", "Thomas", UtilisateurType.ADMIN);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthInterface authService;

    // Requis par le JwtInterceptor enregistré dans WebConfig.
    @MockitoBean
    private JwtService jwtService;

    @Test
    void afficherLogin_sansSession_afficheLeFormulaire() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void afficherLogin_dejaConnecte_redirigeVersLAccueil() throws Exception {
        mockMvc.perform(get("/login").sessionAttr(UtilisateurConnecte.ATTRIBUT, THOMAS))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accueil"));
    }

    @Test
    void connecter_identifiantsValides_ouvreLaSessionEtRedirige() throws Exception {
        when(authService.authentifier("thomas", "1234")).thenReturn(THOMAS);

        MvcResult resultat = mockMvc.perform(post("/login")
                        .param("identifiantConnexion", "thomas")
                        .param("motDePasse", "1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accueil"))
                .andReturn();

        assertThat(resultat.getRequest().getSession().getAttribute(UtilisateurConnecte.ATTRIBUT)).isEqualTo(THOMAS);
    }

    @Test
    void connecter_identifiantsInvalides_reafficheLaPageAvecLeMessageDErreur() throws Exception {
        when(authService.authentifier("thomas", "mauvais")).thenThrow(new IdentifiantsInvalidesException());

        mockMvc.perform(post("/login")
                        .param("identifiantConnexion", "thomas")
                        .param("motDePasse", "mauvais"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("erreur", "Identifiant ou mot de passe incorrect"))
                .andExpect(model().attribute("identifiantConnexion", "thomas"))
                .andExpect(content().string(containsString("Identifiant ou mot de passe incorrect")));
    }

    @Test
    void deconnecter_fermeLaSessionEtAfficheLaPageDeConnexion() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(UtilisateurConnecte.ATTRIBUT, THOMAS);

        mockMvc.perform(get("/deconnexion").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?deconnecte"));

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void afficherLogin_apresDeconnexion_afficheLeMessageDeConfirmation() throws Exception {
        mockMvc.perform(get("/login").param("deconnecte", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Vous avez été déconnecté.")));
    }
}
