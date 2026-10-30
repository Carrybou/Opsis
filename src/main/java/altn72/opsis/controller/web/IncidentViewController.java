package altn72.opsis.controller.web;

import altn72.opsis.dto.incident.IncidentRequest;
import altn72.opsis.dto.incident.IncidentResponse;
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
    public String afficherFormulaire(@PathVariable Long idEquipement, Model model) {
        model.addAttribute("equipement", equipementService.trouver(idEquipement));
        model.addAttribute("incidentRequest", new IncidentRequest(idEquipement, ""));
        return "incidents/declarer";
    }

    @PostMapping("/incidents")
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
}