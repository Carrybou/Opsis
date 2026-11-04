package altn72.opsis.integration;

import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.UtilisateurConnecte;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// F12 : la documentation décrit l'API, et seulement elle ; comme les pages, elle exige d'être connecté (RG11).
@SpringBootTest
@AutoConfigureMockMvc
class DocumentationApiIT extends AvecBaseMysql {

    private static final UtilisateurConnecte AGENT = new UtilisateurConnecte(8L, "agent.test", "Sacha", UtilisateurType.AGENT);

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sansSession_laDocumentationRedirigeVersLaConnexion() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void connecte_laDocumentationDecritLApiEtSonJeton() throws Exception {
        mockMvc.perform(get("/v3/api-docs").sessionAttr(UtilisateurConnecte.ATTRIBUT, AGENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/incidents'].post.summary").value("Déclarer un incident"))
                .andExpect(jsonPath("$.paths['/api/incidents/{id}/resolution'].post.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/api/incidents/{id}/resolution'].post.parameters[?(@.name == 'utilisateur')]").isEmpty())
                .andExpect(jsonPath("$.paths['/stations']").doesNotExist())
                .andExpect(jsonPath("$.components.securitySchemes.jeton.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.security").isEmpty());
    }

    @Test
    void connecte_lInterfaceSwaggerSAffiche() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html").sessionAttr(UtilisateurConnecte.ATTRIBUT, AGENT))
                .andExpect(status().isOk());
    }
}
