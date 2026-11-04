package altn72.opsis.integration;

import altn72.opsis.model.equipement.EquipementEtat;
import altn72.opsis.model.equipement.EquipementRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Toute la pile sans simulation (contrôleur, intercepteur JWT, service, base) sur la vie complète d'un incident,
// avec les comptes de test de bdd.sql. L'équipement 9 et le prestataire 5 ne servent pas aux autres tests d'intégration.
@SpringBootTest
@AutoConfigureMockMvc
class ParcoursIncidentIT extends AvecBaseMysql {

    private static final long EQUIPEMENT = 9L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EquipementRepository equipementRepository;

    @Test
    void parcoursComplet_declaration_affectation_resolution_evaluation() throws Exception {
        String agent = connecter("agent.test");
        String technicien = connecter("technicien.test");
        String responsable = connecter("responsable.test");

        // RG1 : déclaration par un agent, l'équipement tombe en panne ; RG11 : le technicien n'y a pas droit.
        String declaration = "{\"idEquipement\":" + EQUIPEMENT + ",\"description\":\"Escalator à l'arrêt depuis ce matin\"}";
        envoyer(post("/api/incidents"), technicien, declaration).andExpect(status().isForbidden());
        String reponse = envoyer(post("/api/incidents"), agent, declaration)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("OUVERT"))
                .andExpect(jsonPath("$.dateHeureOuverture").isNotEmpty())
                .andExpect(jsonPath("$.nomPrestataire").value(nullValue()))
                .andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(reponse, "$.identifiant");
        verifierEtatEquipement(EquipementEtat.EN_PANNE);

        // RG8 : pas d'évaluation avant la résolution.
        envoyer(post("/api/incidents/" + id + "/evaluation"), responsable, "{\"note\":4}").andExpect(status().isConflict());

        // RG2 : l'affectation passe l'incident à affecté et l'équipement en maintenance ; RG11 : réservée au responsable.
        envoyer(put("/api/incidents/" + id + "/prestataire"), agent, "{\"idPrestataire\":5}").andExpect(status().isForbidden());
        envoyer(put("/api/incidents/" + id + "/prestataire"), responsable, "{\"idPrestataire\":5}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("EN_COURS"));
        verifierEtatEquipement(EquipementEtat.EN_MAINTENANCE);

        // RG5 : la résolution est datée, l'équipement repart ; ensuite plus rien ne bouge.
        envoyer(post("/api/incidents/" + id + "/resolution"), agent, "").andExpect(status().isForbidden());
        envoyer(post("/api/incidents/" + id + "/resolution"), technicien, "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolu").value(true))
                .andExpect(jsonPath("$.dateHeureResolution").isNotEmpty());
        verifierEtatEquipement(EquipementEtat.FONCTIONNEL);
        envoyer(post("/api/incidents/" + id + "/resolution"), technicien, "").andExpect(status().isConflict());
        envoyer(put("/api/incidents/" + id + "/prestataire"), responsable, "{\"idPrestataire\":1}").andExpect(status().isConflict());
        envoyer(patch("/api/incidents/" + id), responsable, "{\"description\":\"Tentative après la résolution\"}").andExpect(status().isForbidden());

        // RG7 : le dernier incident résolu arrive en tête de l'historique.
        mockMvc.perform(get("/api/incidents/historique").header(HttpHeaders.AUTHORIZATION, "Bearer " + agent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].identifiant").value(id));

        // RG8 : une note entière de 1 à 5, une seule fois.
        envoyer(post("/api/incidents/" + id + "/evaluation"), responsable, "{\"note\":4.5}").andExpect(status().isBadRequest());
        envoyer(post("/api/incidents/" + id + "/evaluation"), responsable, "{\"note\":6}").andExpect(status().isBadRequest());
        envoyer(post("/api/incidents/" + id + "/evaluation"), technicien, "{\"note\":4}").andExpect(status().isForbidden());
        envoyer(post("/api/incidents/" + id + "/evaluation"), responsable, "{\"note\":4}").andExpect(status().isCreated());
        envoyer(post("/api/incidents/" + id + "/evaluation"), responsable, "{\"note\":5}").andExpect(status().isConflict());
    }

    @Test
    void sansToken_lApiRepond401() throws Exception {
        mockMvc.perform(get("/api/incidents/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void incidentInexistant_lApiRepond404() throws Exception {
        mockMvc.perform(get("/api/incidents/999999").header(HttpHeaders.AUTHORIZATION, "Bearer " + connecter("agent.test")))
                .andExpect(status().isNotFound());
    }

    private String connecter(String identifiant) throws Exception {
        String reponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifiantConnexion\":\"" + identifiant + "\",\"motDePasse\":\"1234\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(reponse, "$.token");
    }

    private ResultActions envoyer(MockHttpServletRequestBuilder requete, String token, String corps) throws Exception {
        return mockMvc.perform(requete
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(corps));
    }

    private void verifierEtatEquipement(EquipementEtat attendu) {
        assertThat(equipementRepository.findById(EQUIPEMENT).orElseThrow().getEtatFonctionnement()).isEqualTo(attendu);
    }
}
