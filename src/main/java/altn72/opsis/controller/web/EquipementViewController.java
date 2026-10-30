package altn72.opsis.controller.web;

import altn72.opsis.security.UtilisateurConnecte;
import altn72.opsis.service.equipement.EquipementInterface;
import altn72.opsis.service.incident.IncidentInterface;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class EquipementViewController {

    private final EquipementInterface equipementService;
    private final IncidentInterface incidentService;

    public EquipementViewController(EquipementInterface equipementService, IncidentInterface incidentService) {
        this.equipementService = equipementService;
        this.incidentService = incidentService;
    }

    @GetMapping("/equipements/{id}")
    public String afficherFiche(@PathVariable Long id,
                                @SessionAttribute(UtilisateurConnecte.ATTRIBUT) UtilisateurConnecte utilisateur,
                                Model model) {
        model.addAttribute("fiche", equipementService.consulterFiche(id));
        model.addAttribute("idEquipement", id);
        model.addAttribute("incidentsModifiables", incidentService.incidentsModifiables(id, utilisateur));
        return "equipement";
    }
}
