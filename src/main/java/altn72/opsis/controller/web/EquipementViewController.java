package altn72.opsis.controller.web;

import altn72.opsis.service.equipement.EquipementInterface;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class EquipementViewController {

    private final EquipementInterface equipementService;

    public EquipementViewController(EquipementInterface equipementService) {
        this.equipementService = equipementService;
    }

    @GetMapping("/equipements/{id}")
    public String afficherFiche(@PathVariable Long id, Model model) {
        model.addAttribute("fiche", equipementService.consulterFiche(id));
        return "equipement";
    }
}
