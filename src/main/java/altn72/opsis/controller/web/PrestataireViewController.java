package altn72.opsis.controller.web;

import altn72.opsis.service.prestataire.PrestataireInterface;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Consultation des prestataires (F10 : la note est consultable sur la fiche) : lecture seule, ouverte à tous les profils connectés.
@Controller
public class PrestataireViewController {

    private final PrestataireInterface prestataireService;

    public PrestataireViewController(PrestataireInterface prestataireService) {
        this.prestataireService = prestataireService;
    }

    @GetMapping("/prestataires")
    public String lister(Model model) {
        model.addAttribute("prestataires", prestataireService.lister());
        return "prestataires/liste";
    }

    @GetMapping("/prestataires/{id}")
    public String afficherFiche(@PathVariable Long id, Model model) {
        model.addAttribute("fiche", prestataireService.consulterFiche(id));
        return "prestataires/fiche";
    }
}
