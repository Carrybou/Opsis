package altn72.opsis.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice(basePackages = "altn72.opsis.controller.web")
public class VueExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(VueExceptionHandler.class);

    @ExceptionHandler(RessourceIntrouvableException.class)
    public String handleIntrouvable(RessourceIntrouvableException e, HttpServletResponse response, Model model) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        model.addAttribute("titre", "Élément introuvable");
        model.addAttribute("message", e.getMessage());
        return "erreur";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleParametreInvalide(MethodArgumentTypeMismatchException e, HttpServletResponse response, Model model) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        model.addAttribute("titre", "Adresse invalide");
        model.addAttribute("message", "Le paramètre « " + e.getName() + " » n'est pas valide.");
        return "erreur";
    }

    @ExceptionHandler(Exception.class)
    public String handleInattendue(Exception e, HttpServletResponse response, Model model) {
        log.error("Erreur inattendue sur une vue", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("titre", "Une erreur est survenue");
        model.addAttribute("message", "Un problème technique empêche d'afficher cette page. Réessayez dans quelques instants.");
        return "erreur";
    }
}