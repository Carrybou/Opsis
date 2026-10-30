package altn72.opsis.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccueilViewController {

    @GetMapping({"/", "/accueil"})
    public String accueil() {
        return "redirect:/stations";
    }
}
