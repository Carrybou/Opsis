package altn72.opsis.controller.web;

import altn72.opsis.exception.IdentifiantsInvalidesException;
import altn72.opsis.security.UtilisateurConnecte;
import altn72.opsis.service.auth.AuthInterface;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginViewController {

    private final AuthInterface authService;

    public LoginViewController(AuthInterface authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String afficherLogin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(UtilisateurConnecte.ATTRIBUT) != null) {
            return "redirect:/accueil";
        }
        return "login";
    }

    @PostMapping("/login")
    public String connecter(@RequestParam(defaultValue = "") String identifiantConnexion,
                            @RequestParam(defaultValue = "") String motDePasse,
                            HttpServletRequest request,
                            Model model) {
        try {
            UtilisateurConnecte utilisateur = authService.authentifier(identifiantConnexion, motDePasse);

            HttpSession ancienneSession = request.getSession(false);
            if (ancienneSession != null) {
                ancienneSession.invalidate();
            }
            request.getSession(true).setAttribute(UtilisateurConnecte.ATTRIBUT, utilisateur);

            return "redirect:/accueil";
        } catch (IdentifiantsInvalidesException e) {
            model.addAttribute("erreur", e.getMessage());
            model.addAttribute("identifiantConnexion", identifiantConnexion);
            return "login";
        }
    }

    @GetMapping("/deconnexion")
    public String deconnecter(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login?deconnecte";
    }
}