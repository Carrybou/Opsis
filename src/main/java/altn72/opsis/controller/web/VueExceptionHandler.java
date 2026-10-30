package altn72.opsis.controller.web;

import altn72.opsis.exception.RessourceIntrouvableException;
import altn72.opsis.security.UtilisateurConnecte;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttribute;

@ControllerAdvice(basePackages = "altn72.opsis.controller.web")
public class VueExceptionHandler {

    // l'utilisateur connecté est relu ici pour que l'en-tête reste affiché sur la page d'erreur.
    @ExceptionHandler(RessourceIntrouvableException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleRessourceIntrouvable(
            RessourceIntrouvableException e,
            @SessionAttribute(name = UtilisateurConnecte.ATTRIBUT, required = false) UtilisateurConnecte utilisateurConnecte,
            Model model) {
        model.addAttribute(UtilisateurConnecte.ATTRIBUT, utilisateurConnecte);
        model.addAttribute("message", e.getMessage());
        return "erreur";
    }
}
