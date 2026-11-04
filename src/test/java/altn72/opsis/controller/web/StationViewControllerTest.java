package altn72.opsis.controller.web;

import altn72.opsis.dto.equipement.EquipementResponse;
import altn72.opsis.dto.station.ReseauStationsResponse;
import altn72.opsis.dto.station.StationResponse;
import altn72.opsis.model.equipement.EquipementEtat;
import altn72.opsis.model.equipement.EquipementType;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.JwtService;
import altn72.opsis.security.UtilisateurConnecte;
import altn72.opsis.service.station.StationInterface;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest({StationViewController.class, AccueilViewController.class})
class StationViewControllerTest {

    private static final UtilisateurConnecte THOMAS =
            new UtilisateurConnecte(7L, "thomas", "Thomas", UtilisateurType.ADMIN);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StationInterface stationService;

    // Requis par le JwtInterceptor enregistré dans WebConfig.
    @MockitoBean
    private JwtService jwtService;

    @Test
    void afficherStations_sansSession_redirigeVersLaConnexion() throws Exception {
        mockMvc.perform(get("/stations"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void afficherStations_afficheLesEquipementsDeChaqueStation() throws Exception {
        when(stationService.consulterReseau()).thenReturn(new ReseauStationsResponse(
                List.of(new StationResponse(1L, "Châtelet", null, null), new StationResponse(2L, "Gare de Lyon", null, null)),
                Map.of(1L, List.of(new EquipementResponse(4L, "ASC-CHT-001", "Ascenseur", EquipementType.ASCENSEUR, EquipementEtat.EN_PANNE, "Châtelet"))),
                1, 1, 0));

        mockMvc.perform(get("/stations").sessionAttr(UtilisateurConnecte.ATTRIBUT, THOMAS))
                .andExpect(status().isOk())
                .andExpect(view().name("stations"))
                .andExpect(content().string(containsString("href=\"/equipements/4\"")))
                .andExpect(content().string(containsString("Aucun équipement enregistré pour cette station.")));
    }
}
