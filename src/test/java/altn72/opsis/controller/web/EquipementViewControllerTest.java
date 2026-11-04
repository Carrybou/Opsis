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

    private static final UtilisateurConnecte AGENT =
            new UtilisateurConnecte(4L, "agent.bernard", "Hugo", UtilisateurType.AGENT);

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

    // RG3 / RG4 : le badge d'un incident suit son ancienneté tant qu'il n'est pas affecté, y compris hors du tableau de bord.
    @Test
    void afficherFiche_badgeDesIncidents_suitLAncienneteTantQuIlsNeSontPasAffectes() throws Exception {
        LocalDateTime maintenant = LocalDateTime.now();
        when(equipementService.consulterFiche(2L)).thenReturn(new FicheEquipementResponse(2L, "ASC-CHT-001", "Ascenseur accès quai ligne 1",
                EquipementType.ASCENSEUR, EquipementEtat.FONCTIONNEL, 3L, "Châtelet",
                List.of(new IncidentResume(1L, "Récent", IncidentStatut.OUVERT, maintenant.minusHours(47), null, null),
                        new IncidentResume(2L, "Ancien", IncidentStatut.OUVERT, maintenant.minusHours(48), null, null),
                        new IncidentResume(3L, "Très ancien", IncidentStatut.OUVERT, maintenant.minusHours(72), null, null),
                        new IncidentResume(4L, "Pris en charge", IncidentStatut.EN_COURS, maintenant.minusHours(200), null, "AscenTech Services")),
                List.of()));
        when(incidentService.incidentsModifiables(2L, AGENT)).thenReturn(Set.of());

        mockMvc.perform(get("/equipements/2").sessionAttr(UtilisateurConnecte.ATTRIBUT, AGENT))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("badge-vert\">Non affecté")))
                .andExpect(content().string(containsString("badge-alerte\">Non affecté")))
                .andExpect(content().string(containsString("badge-danger\">Non affecté")))
                .andExpect(content().string(containsString("badge-info\">Affecté")));
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
