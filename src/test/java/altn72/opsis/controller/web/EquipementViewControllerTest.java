package altn72.opsis.controller.web;

import altn72.opsis.dto.equipement.FicheEquipementResponse;
import altn72.opsis.dto.incident.IncidentResume;
import altn72.opsis.model.equipement.EquipementEtat;
import altn72.opsis.model.equipement.EquipementType;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.JwtService;
import altn72.opsis.security.UtilisateurConnecte;
import altn72.opsis.service.equipement.EquipementInterface;
import altn72.opsis.service.incident.IncidentInterface;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EquipementViewController.class)
class EquipementViewControllerTest {

    private static final UtilisateurConnecte RESPONSABLE =
            new UtilisateurConnecte(1L, "admin", "Roméo", UtilisateurType.ADMIN);
    private static final UtilisateurConnecte AGENT =
            new UtilisateurConnecte(4L, "agent.bernard", "Hugo", UtilisateurType.AGENT);
    private static final UtilisateurConnecte TECHNICIEN =
            new UtilisateurConnecte(2L, "tech.martin", "Lucas", UtilisateurType.TECHNICIEN);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EquipementInterface equipementService;

    @MockitoBean
    private IncidentInterface incidentService;

    // Requis par le JwtInterceptor enregistré dans WebConfig.
    @MockitoBean
    private JwtService jwtService;

    @Test
    void afficherFiche_agent_peutDeclarerEtNeModifieQueLesIncidentsAutorises() throws Exception {
        when(equipementService.consulterFiche(2L)).thenReturn(fiche());
        when(incidentService.incidentsModifiables(2L, AGENT)).thenReturn(Set.of(1L));

        mockMvc.perform(get("/equipements/2").sessionAttr(UtilisateurConnecte.ATTRIBUT, AGENT))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Déclarer un incident")))
                .andExpect(content().string(containsString("/incidents/1?modifier=true")))
                .andExpect(content().string(not(containsString("/incidents/3?modifier=true"))));
    }

    @Test
    void afficherFiche_responsable_nePeutPasDeclarerMaisPeutModifier() throws Exception {
        when(equipementService.consulterFiche(2L)).thenReturn(fiche());
        when(incidentService.incidentsModifiables(2L, RESPONSABLE)).thenReturn(Set.of(1L, 3L));

        mockMvc.perform(get("/equipements/2").sessionAttr(UtilisateurConnecte.ATTRIBUT, RESPONSABLE))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Déclarer un incident"))))
                .andExpect(content().string(containsString("/incidents/1?modifier=true")))
                .andExpect(content().string(containsString("/incidents/3?modifier=true")));
    }

    @Test
    void afficherFiche_technicien_neVoitNiDeclarationNiModification() throws Exception {
        when(equipementService.consulterFiche(2L)).thenReturn(fiche());
        when(incidentService.incidentsModifiables(2L, TECHNICIEN)).thenReturn(Set.of());

        mockMvc.perform(get("/equipements/2").sessionAttr(UtilisateurConnecte.ATTRIBUT, TECHNICIEN))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Déclarer un incident"))))
                .andExpect(content().string(not(containsString("?modifier=true"))));
    }

    private static FicheEquipementResponse fiche() {
        return new FicheEquipementResponse(2L, "ASC-CHT-001", "Ascenseur accès quai ligne 1",
                EquipementType.ASCENSEUR, EquipementEtat.EN_PANNE, 3L, "Châtelet",
                List.of(new IncidentResume(1L, "Ascenseur bloqué entre deux niveaux", IncidentStatut.OUVERT,
                                LocalDateTime.of(2026, 10, 10, 8, 15), null, null),
                        new IncidentResume(3L, "Portes qui se rouvrent en permanence", IncidentStatut.EN_COURS,
                                LocalDateTime.of(2026, 10, 9, 7, 0), null, "AscenTech Services")),
                List.of());
    }
}
