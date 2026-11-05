package altn72.opsis.controller.web;

import altn72.opsis.dto.carte.CarteReseauResponse;
import altn72.opsis.dto.carte.CarteReseauResponse.EquipementCarte;
import altn72.opsis.dto.carte.CarteReseauResponse.IncidentCarte;
import altn72.opsis.dto.carte.CarteReseauResponse.StationCarte;
import altn72.opsis.model.equipement.EquipementEtat;
import altn72.opsis.model.equipement.EquipementType;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.incident.NiveauAttention;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.JwtService;
import altn72.opsis.security.UtilisateurConnecte;
import altn72.opsis.service.carte.CarteInterface;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarteViewController.class)
class CarteViewControllerTest {

    private static final UtilisateurConnecte TECHNICIEN =
            new UtilisateurConnecte(2L, "tech.martin", "Lucas", UtilisateurType.TECHNICIEN);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarteInterface carteService;

    // Requis par le JwtInterceptor enregistré dans WebConfig.
    @MockitoBean
    private JwtService jwtService;

    @Test
    void afficher_sansSession_redirigeVersLaConnexion() throws Exception {
        mockMvc.perform(get("/carte"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void afficher_donneLesCoordonneesEtLesLiensVersEquipementsEtIncidents() throws Exception {
        StationCarte chatelet = new StationCarte(1L, "Châtelet", new BigDecimal("48.85837200"), new BigDecimal("2.34706100"),
                EquipementEtat.EN_PANNE,
                List.of(new EquipementCarte(2L, "ASC-CHT-001", "Ascenseur accès quai", EquipementType.ASCENSEUR, EquipementEtat.EN_PANNE,
                        List.of(new IncidentCarte(7L, "Ascenseur bloqué", IncidentStatut.OUVERT, LocalDateTime.of(2026, 10, 10, 8, 15), NiveauAttention.ROUGE)))),
                1, 0);
        StationCarte sansCoordonnees = new StationCarte(9L, "Nouvelle station", null, null, null, List.of(), 0, 0);
        when(carteService.consulter()).thenReturn(new CarteReseauResponse(List.of(chatelet, sansCoordonnees), 1, 0, 0, 1));

        mockMvc.perform(get("/carte").sessionAttr(UtilisateurConnecte.ATTRIBUT, TECHNICIEN))
                .andExpect(status().isOk())
                .andExpect(view().name("carte"))
                .andExpect(content().string(allOf(
                        containsString("data-lat=\"48.85837200\""),
                        containsString("data-lng=\"2.34706100\""),
                        containsString("data-situation=\"EN_PANNE\""),
                        containsString("data-situation=\"AUCUNE\""),
                        containsString("href=\"/equipements/2\""),
                        containsString("href=\"/incidents/7\""),
                        containsString("Aucun équipement enregistré pour cette station."))));
    }

    @Test
    void afficher_stationSansCoordonnees_nAPasDeCoordonneesDansLaPage() throws Exception {
        when(carteService.consulter()).thenReturn(new CarteReseauResponse(
                List.of(new StationCarte(9L, "Nouvelle station", null, null, null, List.of(), 0, 0)), 0, 0, 0, 1));

        mockMvc.perform(get("/carte").sessionAttr(UtilisateurConnecte.ATTRIBUT, TECHNICIEN))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("data-lat"))));
    }
}
