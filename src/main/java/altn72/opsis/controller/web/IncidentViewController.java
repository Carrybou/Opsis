package altn72.opsis.controller.web;

import altn72.opsis.dto.incident.IncidentDetailResponse;
import altn72.opsis.dto.incident.IncidentModificationRequest;
import altn72.opsis.dto.incident.IncidentRequest;
import altn72.opsis.dto.incident.IncidentResponse;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.ProfilRequis;
import altn72.opsis.security.UtilisateurConnecte;
import altn72.opsis.service.equipement.EquipementInterface;
import altn72.opsis.service.incident.IncidentInterface;
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

    public IncidentViewController(IncidentInterface incidentService, EquipementInterface equipementService) {
        this.incidentService = incidentService;
        this.equipementService = equipementService;
    }

    @GetMapping("/equipements/{idEquipement}/incidents/nouveau")
    @ProfilRequis(UtilisateurType.AGENT)
    public String afficherFormulaire(@PathVariable Long idEquipement, Model model) {
        model.addAttribute("equipement", equipementService.trouver(idEquipement));
        model.addAttribute("incidentRequest", new IncidentRequest(idEquipement, ""));
        return "incidents/declarer";
    }

    @PostMapping("/incidents")
    @ProfilRequis(UtilisateurType.AGENT)
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

    // F5 + F6 : la modification de la description se fait directement sur la page détail.
    // Le paramètre "modifier" ouvre le formulaire dès l'affichage (lien depuis la fiche équipement, navigateur sans JS).
    @GetMapping("/incidents/{id}")
    public String afficherDetail(@PathVariable Long id,
                                 @RequestParam(defaultValue = "false") boolean modifier,
                                 @SessionAttribute(UtilisateurConnecte.ATTRIBUT) UtilisateurConnecte utilisateur,
                                 Model model) {
        IncidentDetailResponse incident = incidentService.consulterDetail(id);
        boolean modifiable = incidentService.estModifiable(id, utilisateur);
        preparerDetail(incident, modifiable, modifier && modifiable, model);
        model.addAttribute("incidentModificationRequest", new IncidentModificationRequest(incident.description()));
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
            preparerDetail(incidentService.consulterDetail(id), incidentService.estModifiable(id, utilisateur), true, model);
            return "incidents/detail";
        }

        IncidentResponse incident = incidentService.modifier(id, request, utilisateur);

        redirectAttributes.addFlashAttribute("succes", "L'incident n°" + incident.identifiant() + " a bien été modifié.");
        return "redirect:/incidents/" + incident.identifiant();
    }

    private void preparerDetail(IncidentDetailResponse incident, boolean modifiable, boolean enEdition, Model model) {
        model.addAttribute("incident", incident);
        model.addAttribute("modifiable", modifiable);
        model.addAttribute("enEdition", enEdition);
    }
}
