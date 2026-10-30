package altn72.opsis.controller.web;

import altn72.opsis.dto.incident.AffectationRequest;
import altn72.opsis.dto.incident.IncidentDetailResponse;
import altn72.opsis.dto.incident.IncidentModificationRequest;
import altn72.opsis.dto.incident.IncidentResponse;
import altn72.opsis.dto.prestataire.PrestataireResponse;
import altn72.opsis.exception.AffectationIncidentImpossibleException;
import altn72.opsis.exception.ModificationIncidentInterditeException;
import altn72.opsis.model.equipement.EquipementType;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.JwtService;
import altn72.opsis.security.UtilisateurConnecte;
import altn72.opsis.service.equipement.EquipementInterface;
import altn72.opsis.service.incident.IncidentInterface;
import altn72.opsis.service.prestataire.PrestataireInterface;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(IncidentViewController.class)
class IncidentViewControllerTest {

    private static final UtilisateurConnecte RESPONSABLE =
            new UtilisateurConnecte(1L, "admin", "Roméo", UtilisateurType.ADMIN);
    private static final UtilisateurConnecte AGENT =
            new UtilisateurConnecte(4L, "agent.bernard", "Hugo", UtilisateurType.AGENT);
    private static final UtilisateurConnecte TECHNICIEN =
            new UtilisateurConnecte(2L, "tech.martin", "Lucas", UtilisateurType.TECHNICIEN);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IncidentInterface incidentService;

    @MockitoBean
    private EquipementInterface equipementService;

    @MockitoBean
    private PrestataireInterface prestataireService;

    // Requis par le JwtInterceptor enregistré dans WebConfig.
    @MockitoBean
    private JwtService jwtService;

    @Test
    void afficherDetail_incidentModifiable_proposeLeBoutonModifierAvecFormulaireMasque() throws Exception {
        when(incidentService.consulterDetail(1L)).thenReturn(detail());
        when(incidentService.estModifiable(1L, AGENT)).thenReturn(true);

        mockMvc.perform(get("/incidents/1").sessionAttr(UtilisateurConnecte.ATTRIBUT, AGENT))
                .andExpect(status().isOk())
                .andExpect(view().name("incidents/detail"))
                .andExpect(model().attribute("modifiable", true))
                .andExpect(model().attribute("enEdition", false))
                .andExpect(model().attribute("incidentModificationRequest",
                        hasProperty("description", is("Ascenseur bloqué entre deux niveaux"))))
                .andExpect(content().string(containsString("id=\"bouton-modifier\"")))
                .andExpect(content().string(containsString("id=\"formulaire-modification\"")));
    }

    @Test
    void afficherDetail_parametreModifier_ouvreLeFormulaire() throws Exception {
        when(incidentService.consulterDetail(1L)).thenReturn(detail());
        when(incidentService.estModifiable(1L, AGENT)).thenReturn(true);

        mockMvc.perform(get("/incidents/1").param("modifier", "true").sessionAttr(UtilisateurConnecte.ATTRIBUT, AGENT))
                .andExpect(status().isOk())
                .andExpect(model().attribute("enEdition", true));
    }

    @Test
    void afficherDetail_incidentNonModifiable_nAfficheNiBoutonNiFormulaire() throws Exception {
        when(incidentService.consulterDetail(1L)).thenReturn(detail());
        when(incidentService.estModifiable(1L, TECHNICIEN)).thenReturn(false);

        mockMvc.perform(get("/incidents/1").param("modifier", "true").sessionAttr(UtilisateurConnecte.ATTRIBUT, TECHNICIEN))
                .andExpect(status().isOk())
                .andExpect(model().attribute("enEdition", false))
                .andExpect(content().string(not(containsString("formulaire-modification"))))
                .andExpect(content().string(not(containsString("bouton-modifier"))));
    }

    @Test
    void modifier_technicien_estRefuseParLaRoute() throws Exception {
        mockMvc.perform(put("/incidents/1")
                        .sessionAttr(UtilisateurConnecte.ATTRIBUT, TECHNICIEN)
                        .param("description", "Ascenseur bloqué entre deux niveaux"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(incidentService);
    }

    @Test
    void modifier_regleMetierNonRespectee_afficheLaPageDErreur403() throws Exception {
        when(incidentService.modifier(eq(1L), any(IncidentModificationRequest.class), eq(AGENT)))
                .thenThrow(new ModificationIncidentInterditeException("Vous ne pouvez modifier que les incidents que vous avez déclarés"));

        mockMvc.perform(put("/incidents/1")
                        .sessionAttr(UtilisateurConnecte.ATTRIBUT, AGENT)
                        .param("description", "Ascenseur bloqué entre deux niveaux"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("erreur"))
                .andExpect(content().string(containsString("Vous ne pouvez modifier que les incidents que vous avez déclarés")));
    }

    @Test
    void modifier_descriptionValide_enregistreEtResteSurLaPageDetail() throws Exception {
        when(incidentService.modifier(eq(1L), any(IncidentModificationRequest.class), eq(RESPONSABLE))).thenReturn(incident());

        mockMvc.perform(put("/incidents/1")
                        .sessionAttr(UtilisateurConnecte.ATTRIBUT, RESPONSABLE)
                        .param("description", "Ascenseur bloqué entre deux niveaux"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/incidents/1"))
                .andExpect(flash().attribute("succes", "L'incident n°1 a bien été modifié."));
    }

    @Test
    void modifier_descriptionTropCourte_reafficheLeDetailAvecLeFormulaireOuvert() throws Exception {
        when(incidentService.consulterDetail(1L)).thenReturn(detail());
        when(incidentService.estModifiable(1L, RESPONSABLE)).thenReturn(true);

        mockMvc.perform(put("/incidents/1")
                        .sessionAttr(UtilisateurConnecte.ATTRIBUT, RESPONSABLE)
                        .param("description", "court"))
                .andExpect(status().isOk())
                .andExpect(view().name("incidents/detail"))
                .andExpect(model().attribute("enEdition", true))
                .andExpect(model().attributeHasFieldErrors("incidentModificationRequest", "description"))
                .andExpect(content().string(containsString("La description doit contenir entre 10 et 2000 caractères")));

        verify(incidentService, never()).modifier(any(), any(), any());
    }

    @Test
    void afficherDetail_sansSession_redirigeVersLaConnexion() throws Exception {
        mockMvc.perform(get("/incidents/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(incidentService);
    }

    @Test
    void declarer_responsableEnvoieLeFormulaire_estRefuseParLaRoute() throws Exception {
        mockMvc.perform(post("/incidents")
                        .sessionAttr(UtilisateurConnecte.ATTRIBUT, RESPONSABLE)
                        .param("idEquipement", "2")
                        .param("description", "Ascenseur bloqué entre deux niveaux"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(incidentService);
    }

    @Test
    void declarer_technicien_estRefuseParLaRoute() throws Exception {
        mockMvc.perform(get("/equipements/2/incidents/nouveau").sessionAttr(UtilisateurConnecte.ATTRIBUT, TECHNICIEN))
                .andExpect(status().isForbidden());

        verifyNoInteractions(equipementService);
    }

    @Test
    void afficherDetail_responsable_proposeLAffectationAvecLaListeDesPrestataires() throws Exception {
        when(incidentService.consulterDetail(1L)).thenReturn(detail());
        when(incidentService.estAffectable(1L, RESPONSABLE)).thenReturn(true);
        when(prestataireService.lister()).thenReturn(prestataires());

        mockMvc.perform(get("/incidents/1").sessionAttr(UtilisateurConnecte.ATTRIBUT, RESPONSABLE))
                .andExpect(status().isOk())
                .andExpect(model().attribute("affectable", true))
                .andExpect(model().attribute("enAffectation", false))
                .andExpect(content().string(containsString("id=\"bouton-affecter\"")))
                .andExpect(content().string(containsString("Mobilis Maintenance")));
    }

    @Test
    void afficherDetail_parametreAffecter_ouvreLeFormulaireDAffectation() throws Exception {
        when(incidentService.consulterDetail(1L)).thenReturn(detail());
        when(incidentService.estAffectable(1L, RESPONSABLE)).thenReturn(true);
        when(prestataireService.lister()).thenReturn(prestataires());

        mockMvc.perform(get("/incidents/1").param("affecter", "true").sessionAttr(UtilisateurConnecte.ATTRIBUT, RESPONSABLE))
                .andExpect(status().isOk())
                .andExpect(model().attribute("enAffectation", true));
    }

    @Test
    void afficherDetail_agent_nAPasAccesALAffectation() throws Exception {
        when(incidentService.consulterDetail(1L)).thenReturn(detail());
        when(incidentService.estAffectable(1L, AGENT)).thenReturn(false);

        mockMvc.perform(get("/incidents/1").param("affecter", "true").sessionAttr(UtilisateurConnecte.ATTRIBUT, AGENT))
                .andExpect(status().isOk())
                .andExpect(model().attribute("enAffectation", false))
                .andExpect(content().string(not(containsString("bouton-affecter"))))
                .andExpect(content().string(not(containsString("formulaire-affectation"))));

        verifyNoInteractions(prestataireService);
    }

    @Test
    void affecter_agentOuTechnicien_estRefuseParLaRoute() throws Exception {
        for (UtilisateurConnecte utilisateur : List.of(AGENT, TECHNICIEN)) {
            mockMvc.perform(put("/incidents/1/prestataire")
                            .sessionAttr(UtilisateurConnecte.ATTRIBUT, utilisateur)
                            .param("idPrestataire", "2"))
                    .andExpect(status().isForbidden());
        }

        verifyNoInteractions(incidentService);
    }

    @Test
    void affecter_responsable_enregistreEtResteSurLaPageDetail() throws Exception {
        when(incidentService.affecter(eq(1L), any(AffectationRequest.class), eq(RESPONSABLE))).thenReturn(incidentAffecte());

        mockMvc.perform(put("/incidents/1/prestataire")
                        .sessionAttr(UtilisateurConnecte.ATTRIBUT, RESPONSABLE)
                        .param("idPrestataire", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/incidents/1"))
                .andExpect(flash().attribute("succes", "L'incident n°1 est maintenant affecté à Mobilis Maintenance."));
    }

    @Test
    void affecter_sansPrestataireChoisi_reafficheLeDetailAvecLeFormulaireOuvert() throws Exception {
        when(incidentService.consulterDetail(1L)).thenReturn(detail());
        when(incidentService.estAffectable(1L, RESPONSABLE)).thenReturn(true);
        when(prestataireService.lister()).thenReturn(prestataires());

        mockMvc.perform(put("/incidents/1/prestataire")
                        .sessionAttr(UtilisateurConnecte.ATTRIBUT, RESPONSABLE)
                        .param("idPrestataire", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("incidents/detail"))
                .andExpect(model().attribute("enAffectation", true))
                .andExpect(model().attributeHasFieldErrors("affectationRequest", "idPrestataire"))
                .andExpect(content().string(containsString("Veuillez choisir un prestataire")));

        verify(incidentService, never()).affecter(any(), any(), any());
    }

    @Test
    void affecter_incidentResolu_afficheLaPageDErreur409() throws Exception {
        when(incidentService.affecter(eq(6L), any(AffectationRequest.class), eq(RESPONSABLE)))
                .thenThrow(new AffectationIncidentImpossibleException("L'incident n°6 est résolu : il ne peut plus être affecté ni réaffecté"));

        mockMvc.perform(put("/incidents/6/prestataire")
                        .sessionAttr(UtilisateurConnecte.ATTRIBUT, RESPONSABLE)
                        .param("idPrestataire", "2"))
                .andExpect(status().isConflict())
                .andExpect(view().name("erreur"))
                .andExpect(content().string(containsString("ne peut plus être affecté ni réaffecté")));
    }

    @Test
    void declarer_responsable_estRefuseParLaRoute() throws Exception {
        mockMvc.perform(get("/equipements/2/incidents/nouveau").sessionAttr(UtilisateurConnecte.ATTRIBUT, RESPONSABLE))
                .andExpect(status().isForbidden());

        verifyNoInteractions(equipementService);
    }

    private static IncidentDetailResponse detail() {
        return new IncidentDetailResponse(1L, "Ascenseur bloqué entre deux niveaux", IncidentStatut.OUVERT,
                LocalDateTime.of(2026, 10, 10, 8, 15), null, false, "2 h 0 min",
                2L, "ASC-CHT-001", "Ascenseur accès quai ligne 1", EquipementType.ASCENSEUR,
                3L, "Châtelet", null, null, null, "Hugo", null);
    }

    private static List<PrestataireResponse> prestataires() {
        return List.of(new PrestataireResponse(1L, "AscenTech Services", "astreinte@ascentech-exemple.fr"),
                new PrestataireResponse(2L, "Mobilis Maintenance", "01 45 67 89 10"));
    }

    private static IncidentResponse incidentAffecte() {
        return new IncidentResponse(1L, "Ascenseur bloqué entre deux niveaux", IncidentStatut.EN_COURS,
                LocalDateTime.of(2026, 10, 10, 8, 15), null,
                2L, "ASC-CHT-001", "Ascenseur accès quai ligne 1", "Châtelet",
                2L, "Mobilis Maintenance", 4L, "Hugo");
    }

    private static IncidentResponse incident() {
        return new IncidentResponse(1L, "Ascenseur bloqué entre deux niveaux", IncidentStatut.OUVERT,
                LocalDateTime.of(2026, 10, 10, 8, 15), null,
                2L, "ASC-CHT-001", "Ascenseur accès quai ligne 1", "Châtelet",
                null, null, 4L, "Hugo");
    }
}
