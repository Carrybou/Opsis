package altn72.opsis.controller.web;

import altn72.opsis.dto.evaluation.EvaluationRequest;
import altn72.opsis.dto.evaluation.EvaluationResponse;
import altn72.opsis.dto.incident.AffectationRequest;
import altn72.opsis.dto.incident.IncidentDetailResponse;
import altn72.opsis.dto.incident.IncidentModificationRequest;
import altn72.opsis.dto.incident.IncidentRequest;
import altn72.opsis.dto.incident.IncidentResponse;
import altn72.opsis.exception.IncidentDejaResoluException;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.ProfilRequis;
import altn72.opsis.security.UtilisateurConnecte;
import altn72.opsis.service.equipement.EquipementInterface;
import altn72.opsis.service.evaluation.EvaluationInterface;
import altn72.opsis.service.incident.IncidentInterface;
import altn72.opsis.service.prestataire.PrestataireInterface;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class IncidentViewController {

    private final IncidentInterface incidentService;
    private final EquipementInterface equipementService;
    private final PrestataireInterface prestataireService;
    private final EvaluationInterface evaluationService;

    public IncidentViewController(IncidentInterface incidentService, EquipementInterface equipementService,
                                  PrestataireInterface prestataireService, EvaluationInterface evaluationService) {
        this.incidentService = incidentService;
        this.equipementService = equipementService;
        this.prestataireService = prestataireService;
        this.evaluationService = evaluationService;
    }

    @GetMapping("/equipements/{idEquipement}/incidents/nouveau")
    @ProfilRequis({UtilisateurType.AGENT, UtilisateurType.ADMIN})
    public String afficherFormulaire(@PathVariable Long idEquipement, Model model) {
        model.addAttribute("equipement", equipementService.trouver(idEquipement));
        model.addAttribute("incidentRequest", new IncidentRequest(idEquipement, ""));
        return "incidents/declarer";
    }

    @PostMapping("/incidents")
    @ProfilRequis({UtilisateurType.AGENT, UtilisateurType.ADMIN})
    public String declarer(@Valid @ModelAttribute("incidentRequest") IncidentRequest request,
                           BindingResult resultat,
                           @SessionAttribute(UtilisateurConnecte.ATTRIBUT) UtilisateurConnecte utilisateur,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (request.getIdEquipement() == null) {
            return "redirect:/accueil";
        }

        if (resultat.hasErrors()) {
            model.addAttribute("equipement", equipementService.trouver(request.getIdEquipement()));
            return "incidents/declarer";
        }

        IncidentResponse incident = incidentService.declarer(request, utilisateur.id());

        redirectAttributes.addFlashAttribute("succes",
                "L'incident n°" + incident.identifiant() + " a bien été déclaré sur l'équipement "
                        + incident.referenceEquipement() + ".");
        return "redirect:/equipements/" + incident.idEquipement();
    }

    @GetMapping("/incidents/{id}")
    public String afficherDetail(@PathVariable Long id,
                                 @RequestParam(defaultValue = "false") boolean modifier,
                                 @RequestParam(defaultValue = "false") boolean affecter,
                                 @RequestParam(defaultValue = "false") boolean evaluer,
                                 @SessionAttribute(UtilisateurConnecte.ATTRIBUT) UtilisateurConnecte utilisateur,
                                 Model model) {
        preparerDetail(id, utilisateur, modifier, affecter, evaluer, model);
        return "incidents/detail";
    }

    @PutMapping("/incidents/{id}")
    @ProfilRequis({UtilisateurType.ADMIN, UtilisateurType.AGENT})
    public String modifier(@PathVariable Long id,
                           @Valid @ModelAttribute("incidentModificationRequest") IncidentModificationRequest request,
                           BindingResult resultat,
                           @SessionAttribute(UtilisateurConnecte.ATTRIBUT) UtilisateurConnecte utilisateur,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (resultat.hasErrors()) {
            preparerDetail(id, utilisateur, true, false, false, model);
            return "incidents/detail";
        }

        IncidentResponse incident = incidentService.modifier(id, request, utilisateur);

        redirectAttributes.addFlashAttribute("succes", "L'incident n°" + incident.identifiant() + " a bien été modifié.");
        return "redirect:/incidents/" + incident.identifiant();
    }

    @PutMapping("/incidents/{id}/prestataire")
    @ProfilRequis(UtilisateurType.ADMIN)
    public String affecter(@PathVariable Long id,
                           @Valid @ModelAttribute("affectationRequest") AffectationRequest request,
                           BindingResult resultat,
                           @SessionAttribute(UtilisateurConnecte.ATTRIBUT) UtilisateurConnecte utilisateur,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (resultat.hasErrors()) {
            preparerDetail(id, utilisateur, false, true, false, model);
            return "incidents/detail";
        }

        IncidentResponse incident = incidentService.affecter(id, request, utilisateur);

        redirectAttributes.addFlashAttribute("succes", "L'incident n°" + incident.identifiant()
                + " est maintenant affecté à " + incident.nomPrestataire() + ".");
        return "redirect:/incidents/" + incident.identifiant();
    }

    @PostMapping("/incidents/{id}/evaluation")
    @ProfilRequis(UtilisateurType.ADMIN)
    public String evaluer(@PathVariable Long id,
                          @Valid @ModelAttribute("evaluationRequest") EvaluationRequest request,
                          BindingResult resultat,
                          @SessionAttribute(UtilisateurConnecte.ATTRIBUT) UtilisateurConnecte utilisateur,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (resultat.hasErrors()) {
            preparerDetail(id, utilisateur, false, false, true, model);
            return "incidents/detail";
        }

        EvaluationResponse evaluation = evaluationService.evaluer(id, request, utilisateur);

        redirectAttributes.addFlashAttribute("succes", "L'intervention sur l'incident n°" + id
                + " a été évaluée " + evaluation.note() + "/5.");
        return "redirect:/incidents/" + id;
    }

    @PostMapping("/incidents/{id}/resolution")
    @ProfilRequis({UtilisateurType.ADMIN, UtilisateurType.TECHNICIEN})
    public String resoudre(@PathVariable Long id,
                           @SessionAttribute(UtilisateurConnecte.ATTRIBUT) UtilisateurConnecte utilisateur,
                           RedirectAttributes redirectAttributes) {
        try {
            incidentService.resoudre(id, utilisateur);
            redirectAttributes.addFlashAttribute("succes", "L'incident n°" + id + " a été déclaré comme résolu.");
        } catch (IncidentDejaResoluException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/incidents/" + id;
    }

    // Les formulaires déjà présents dans le modèle (saisie invalide renvoyée par @ModelAttribute) sont conservés.
    private void preparerDetail(Long id, UtilisateurConnecte utilisateur, boolean enEdition, boolean enAffectation,
                                boolean enEvaluation, Model model) {
        IncidentDetailResponse incident = incidentService.consulterDetail(id);
        boolean modifiable = incidentService.estModifiable(id, utilisateur);
        boolean affectable = incidentService.estAffectable(id, utilisateur);
        boolean evaluable = evaluationService.estEvaluable(id, utilisateur);

        model.addAttribute("incident", incident);
        model.addAttribute("modifiable", modifiable);
        model.addAttribute("enEdition", enEdition && modifiable);
        model.addAttribute("affectable", affectable);
        model.addAttribute("enAffectation", enAffectation && affectable);
        model.addAttribute("evaluable", evaluable);
        model.addAttribute("enEvaluation", enEvaluation && evaluable);
        if (affectable) {
            model.addAttribute("prestataires", prestataireService.lister());
        }
        if (incident.idPrestataire() != null) {
            model.addAttribute("prestataireActuel", prestataireService.trouver(incident.idPrestataire()));
        }
        if (!model.containsAttribute("incidentModificationRequest")) {
            model.addAttribute("incidentModificationRequest", new IncidentModificationRequest(incident.description()));
        }
        if (!model.containsAttribute("affectationRequest")) {
            model.addAttribute("affectationRequest", new AffectationRequest());
        }
        if (!model.containsAttribute("evaluationRequest")) {
            model.addAttribute("evaluationRequest", new EvaluationRequest());
        }
    }

    @GetMapping("/incidents/historique")
    public String historique(Model model) {
        model.addAttribute("incidents", incidentService.listerHistoriqueResolus());
        model.addAttribute("notes", evaluationService.notesParIncident());
        return "incidents/historique";
    }
}
