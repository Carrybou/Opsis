package altn72.opsis.controller.web;

import altn72.opsis.security.UtilisateurConnecte;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "altn72.opsis.controller.web")
public class VueModelAdvice {

    @ModelAttribute("utilisateurConnecte")
    public UtilisateurConnecte utilisateurConnecte(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (UtilisateurConnecte) session.getAttribute(UtilisateurConnecte.ATTRIBUT);
    }
}