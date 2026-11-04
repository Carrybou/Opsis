package altn72.opsis.controller.web;

import altn72.opsis.dto.tableaudebord.FiltresTableauDeBord;
import altn72.opsis.dto.tableaudebord.TableauDeBordResponse;
import altn72.opsis.dto.tableaudebord.TableauDeBordResponse.Courbe;
import altn72.opsis.dto.tableaudebord.TableauDeBordResponse.Indicateurs;
import altn72.opsis.dto.tableaudebord.TableauDeBordResponse.Point;
import altn72.opsis.model.incident.NiveauAttention;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.JwtService;
import altn72.opsis.security.UtilisateurConnecte;
import altn72.opsis.service.tableaudebord.TableauDeBordInterface;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(TableauDeBordViewController.class)
class TableauDeBordViewControllerTest {

    private static final UtilisateurConnecte RESPONSABLE =
            new UtilisateurConnecte(1L, "admin", "Roméo", UtilisateurType.ADMIN);
    private static final UtilisateurConnecte AGENT =
            new UtilisateurConnecte(4L, "agent.bernard", "Hugo", UtilisateurType.AGENT);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TableauDeBordInterface tableauDeBordService;

    // Requis par le JwtInterceptor enregistré dans WebConfig.
    @MockitoBean
    private JwtService jwtService;

    @Test
    void afficher_agent_estRefuseParLaRoute() throws Exception {
        mockMvc.perform(get("/tableau-de-bord").sessionAttr(UtilisateurConnecte.ATTRIBUT, AGENT))
                .andExpect(status().isForbidden());

        verifyNoInteractions(tableauDeBordService);
    }

    @Test
    void afficher_responsable_transmetLesFiltresDeLUrlAuService() throws Exception {
        FiltresTableauDeBord attendus = new FiltresTableauDeBord(2L, Set.of(NiveauAttention.ROUGE, NiveauAttention.ORANGE),
                null, "ecran", "recent");
        when(tableauDeBordService.consulter(attendus)).thenReturn(vide());

        mockMvc.perform(get("/tableau-de-bord")
                        .param("station", "2").param("niveau", "ROUGE", "ORANGE").param("q", "ecran").param("tri", "recent")
                        .sessionAttr(UtilisateurConnecte.ATTRIBUT, RESPONSABLE))
                .andExpect(status().isOk())
                .andExpect(view().name("tableau-de-bord"));
    }

    private static TableauDeBordResponse vide() {
        // Le template lit le 1er, le 15e et le dernier jour de la courbe : il lui faut 30 points.
        List<Point> trente = Collections.nCopies(30, new Point(LocalDate.of(2026, 11, 4), 0, 0, 100));
        return new TableauDeBordResponse(LocalDateTime.of(2026, 11, 4, 12, 0),
                new Indicateurs(0, 0L, 0, 0, 0, 0, 0, 0, 0),
                Map.of(NiveauAttention.ROUGE, 0L, NiveauAttention.ORANGE, 0L, NiveauAttention.VERT, 0L, NiveauAttention.AFFECTE, 0L),
                List.of(), List.of(), new Courbe(trente, 1, List.of()), List.of(), 0, Map.of());
    }
}
