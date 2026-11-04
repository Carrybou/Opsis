package altn72.opsis.controller.web;

import altn72.opsis.security.UtilisateurConnecte;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class AccueilViewController {

    // Le responsable d'exploitation arrive sur le tableau de bord (F11), les autres profils sur les stations.
    @GetMapping({"/", "/accueil"})
    public String accueil(@SessionAttribute(UtilisateurConnecte.ATTRIBUT) UtilisateurConnecte utilisateur) {
        return utilisateur.peutConsulterTableauDeBord() ? "redirect:/tableau-de-bord" : "redirect:/stations";
    }
}
