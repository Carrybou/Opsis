package altn72.opsis.controller.web;

import altn72.opsis.dto.evaluation.EvaluationRequest;
import altn72.opsis.dto.evaluation.EvaluationResponse;
import altn72.opsis.dto.incident.AffectationRequest;
import altn72.opsis.dto.incident.IncidentDetailResponse;
import altn72.opsis.dto.incident.IncidentModificationRequest;
import altn72.opsis.dto.incident.IncidentResponse;
import altn72.opsis.dto.prestataire.PrestataireResponse;
import altn72.opsis.exception.acces.ModificationIncidentInterditeException;
import altn72.opsis.model.equipement.EquipementType;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.incident.NiveauAttention;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.JwtService;
import altn72.opsis.security.UtilisateurConnecte;
import altn72.opsis.service.equipement.EquipementInterface;
import altn72.opsis.service.evaluation.EvaluationInterface;
import altn72.opsis.service.incident.IncidentInterface;
import altn72.opsis.service.prestataire.PrestataireInterface;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
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

    @MockitoBean
    private EvaluationInterface evaluationService;

    // Requis par le JwtInterceptor enregistré dans WebConfig.
    @MockitoBean
    private JwtService jwtService;

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
    void declarer_responsableEnvoieLeFormulaire_enregistreLIncident() throws Exception {
        when(incidentService.declarer(any(), eq(RESPONSABLE.id()))).thenReturn(incident());

        mockMvc.perform(post("/incidents")
                        .sessionAttr(UtilisateurConnecte.ATTRIBUT, RESPONSABLE)
                        .param("idEquipement", "2")
                        .param("description", "Ascenseur bloqué entre deux niveaux"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipements/2"));
    }

    @Test
    void declarer_technicienEnvoieLeFormulaire_estRefuseParLaRoute() throws Exception {
        mockMvc.perform(post("/incidents")
                        .sessionAttr(UtilisateurConnecte.ATTRIBUT, TECHNICIEN)
                        .param("idEquipement", "2")
                        .param("description", "Ascenseur bloqué entre deux niveaux"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(incidentService);
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
    void resoudre_agent_estRefuseParLaRoute() throws Exception {
        mockMvc.perform(post("/incidents/1/resolution").sessionAttr(UtilisateurConnecte.ATTRIBUT, AGENT))
                .andExpect(status().isForbidden());

        verifyNoInteractions(incidentService);
    }

    @Test
    void resoudre_responsableOuTechnicien_enregistreEtResteSurLaPageDetail() throws Exception {
        for (UtilisateurConnecte autorise : List.of(RESPONSABLE, TECHNICIEN)) {
            mockMvc.perform(post("/incidents/1/resolution").sessionAttr(UtilisateurConnecte.ATTRIBUT, autorise))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/incidents/1"))
                    .andExpect(flash().attribute("succes", "L'incident n°1 a été déclaré comme résolu."));

            verify(incidentService).resoudre(1L, autorise);
        }
    }

    @Test
    void evaluer_agentOuTechnicien_estRefuseParLaRoute() throws Exception {
        for (UtilisateurConnecte utilisateur : List.of(AGENT, TECHNICIEN)) {
            mockMvc.perform(post("/incidents/6/evaluation")
                            .sessionAttr(UtilisateurConnecte.ATTRIBUT, utilisateur)
                            .param("note", "4"))
                    .andExpect(status().isForbidden());
        }

        verifyNoInteractions(evaluationService);
    }

    @Test
    void evaluer_responsable_enregistreEtResteSurLaPageDetail() throws Exception {
        when(evaluationService.evaluer(eq(6L), any(EvaluationRequest.class), eq(RESPONSABLE)))
                .thenReturn(new EvaluationResponse(9L, 4, "Intervention rapide", LocalDate.of(2026, 11, 3)));

        mockMvc.perform(post("/incidents/6/evaluation")
                        .sessionAttr(UtilisateurConnecte.ATTRIBUT, RESPONSABLE)
                        .param("note", "4")
                        .param("commentaire", "Intervention rapide"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/incidents/6"))
                .andExpect(flash().attribute("succes", "L'intervention sur l'incident n°6 a été évaluée 4/5."));
    }

    @Test
    void evaluer_noteHorsBornes_estRefusee() throws Exception {
        when(incidentService.consulterDetail(6L)).thenReturn(detailResoluAffecte());
        when(evaluationService.estEvaluable(6L, RESPONSABLE)).thenReturn(true);
        when(prestataireService.trouver(2L)).thenReturn(prestataires().get(1));

        mockMvc.perform(post("/incidents/6/evaluation")
                        .sessionAttr(UtilisateurConnecte.ATTRIBUT, RESPONSABLE)
                        .param("note", "6"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("evaluationRequest", "note"))
                .andExpect(content().string(containsString("La note doit être comprise entre 1 et 5")));

        verify(evaluationService, never()).evaluer(any(), any(), any());
    }

    // RG10 : deux utilisateurs modifient le même incident en même temps.
    @Test
    void resoudre_modificationSimultanee_afficheLaPageDErreur409() throws Exception {
        when(incidentService.resoudre(1L, TECHNICIEN))
                .thenThrow(new ObjectOptimisticLockingFailureException("Incident", 1L));

        mockMvc.perform(post("/incidents/1/resolution").sessionAttr(UtilisateurConnecte.ATTRIBUT, TECHNICIEN))
                .andExpect(status().isConflict())
                .andExpect(view().name("erreur"))
                .andExpect(content().string(containsString("Modification simultanée")));
    }

    private static IncidentDetailResponse detail() {
        return new IncidentDetailResponse(1L, "Ascenseur bloqué entre deux niveaux", IncidentStatut.OUVERT,
                LocalDateTime.of(2026, 10, 10, 8, 15), null, false, "2 h 0 min",
                2L, "ASC-CHT-001", "Ascenseur accès quai ligne 1", EquipementType.ASCENSEUR,
                3L, "Châtelet", null, null, null, "Hugo", null, NiveauAttention.VERT);
    }

    private static List<PrestataireResponse> prestataires() {
        return List.of(new PrestataireResponse(1L, "AscenTech Services", "astreinte@ascentech-exemple.fr", null, 0),
                new PrestataireResponse(2L, "Mobilis Maintenance", "01 45 67 89 10", 4.5, 2));
    }

    private static IncidentDetailResponse detailResoluAffecte() {
        return new IncidentDetailResponse(6L, "Marche cassée en bas de l'escalator", IncidentStatut.RESOLU,
                LocalDateTime.of(2026, 9, 2, 7, 30), LocalDateTime.of(2026, 9, 3, 14, 0), true, "1 j 6 h",
                1L, "ESC-CHT-001", "Escalator sortie nord", EquipementType.ESCALATOR,
                3L, "Châtelet", 2L, "Mobilis Maintenance", "01 45 67 89 10", "Hugo", null, NiveauAttention.VERT);
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
