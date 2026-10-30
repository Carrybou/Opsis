package altn72.opsis.controller.web;

import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.equipement.EquipementRepository;
import altn72.opsis.model.equipement.EquipementType;
import altn72.opsis.model.station.Station;
import altn72.opsis.model.station.StationRepository;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.JwtService;
import altn72.opsis.security.UtilisateurConnecte;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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
    private StationRepository stationRepository;

    @MockitoBean
    private EquipementRepository equipementRepository;

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
    void accueil_redirigeVersLesStations() throws Exception {
        mockMvc.perform(get("/").sessionAttr(UtilisateurConnecte.ATTRIBUT, THOMAS))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/stations"));
    }

    @Test
    void afficherStations_regroupeLesEquipementsParStationEtCalculeLaSynthese() throws Exception {
        Station chatelet = station(1L, "Châtelet");
        Station gareDeLyon = station(2L, "Gare de Lyon");
        Equipement escalator = equipement(1L, "ESC-CHT-001", "FONCTIONNEL", chatelet);
        Equipement ascenseur = equipement(2L, "ASC-CHT-001", "EN_PANNE", chatelet);
        Equipement ecran = equipement(3L, "ECR-GDL-001", "EN_MAINTENANCE", gareDeLyon);
        when(stationRepository.findAll()).thenReturn(List.of(chatelet, gareDeLyon));
        when(equipementRepository.findAll()).thenReturn(List.of(escalator, ascenseur, ecran));

        MvcResult resultat = mockMvc.perform(get("/stations").sessionAttr(UtilisateurConnecte.ATTRIBUT, THOMAS))
                .andExpect(status().isOk())
                .andExpect(view().name("stations"))
                .andExpect(model().attribute("nombreStations", 2))
                .andExpect(model().attribute("nombreEquipements", 3))
                .andExpect(model().attribute("nombreEnPanne", 1L))
                .andExpect(model().attribute("nombreEnMaintenance", 1L))
                .andReturn();

        @SuppressWarnings("unchecked")
        Map<Long, List<Equipement>> equipementsParStation =
                (Map<Long, List<Equipement>>) resultat.getModelAndView().getModel().get("equipementsParStation");
        assertThat(equipementsParStation.get(1L)).containsExactly(escalator, ascenseur);
        assertThat(equipementsParStation.get(2L)).containsExactly(ecran);
    }

    @Test
    void afficherStations_afficheTypeEtatEtLienVersLaFicheDeChaqueEquipement() throws Exception {
        Station chatelet = station(1L, "Châtelet");
        when(stationRepository.findAll()).thenReturn(List.of(chatelet));
        when(equipementRepository.findAll()).thenReturn(List.of(equipement(2L, "ASC-CHT-001", "EN_PANNE", chatelet)));

        mockMvc.perform(get("/stations").sessionAttr(UtilisateurConnecte.ATTRIBUT, THOMAS))
                .andExpect(content().string(allOf(
                        containsString("Châtelet"),
                        containsString("href=\"/equipements/2\""),
                        containsString("ASCENSEUR"),
                        containsString("En panne"))));
    }

    @Test
    void afficherStations_stationSansEquipement_afficheUnMessage() throws Exception {
        when(stationRepository.findAll()).thenReturn(List.of(station(5L, "Montparnasse")));
        when(equipementRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/stations").sessionAttr(UtilisateurConnecte.ATTRIBUT, THOMAS))
                .andExpect(content().string(containsString("Aucun équipement enregistré pour cette station.")));
    }

    @Test
    void afficherStations_afficheLEnTeteDeLUtilisateurConnecte() throws Exception {
        when(stationRepository.findAll()).thenReturn(List.of());
        when(equipementRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/stations").sessionAttr(UtilisateurConnecte.ATTRIBUT, THOMAS))
                .andExpect(content().string(allOf(
                        containsString("Bonjour <strong>Thomas</strong>"),
                        containsString("(<span>ADMIN</span>)"),
                        containsString("href=\"/deconnexion\">Déconnectez-vous</a>"))));
    }

    private static Station station(Long id, String nom) {
        Station station = new Station();
        station.setId(id);
        station.setNom(nom);
        return station;
    }

    private static Equipement equipement(Long id, String reference, String etat, Station station) {
        Equipement equipement = new Equipement();
        equipement.setId(id);
        equipement.setReferenceUnique(reference);
        equipement.setLibelleDescription("Description " + reference);
        equipement.setType(reference.startsWith("ASC") ? EquipementType.ASCENSEUR : EquipementType.ESCALATOR);
        equipement.setEtatFonctionnement(etat);
        equipement.setIdStation(station);
        return equipement;
    }
}
