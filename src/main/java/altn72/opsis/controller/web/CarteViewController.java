package altn72.opsis.controller.web;

import altn72.opsis.service.carte.CarteInterface;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CarteViewController {

    private final CarteInterface carteService;

    public CarteViewController(CarteInterface carteService) {
        this.carteService = carteService;
    }

    @GetMapping("/carte")
    public String afficher(Model model) {
        model.addAttribute("carte", carteService.consulter());
        return "carte";
    }
}
