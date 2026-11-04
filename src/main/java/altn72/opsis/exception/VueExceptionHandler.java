package altn72.opsis.exception;

import altn72.opsis.security.UtilisateurConnecte;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice(basePackages = "altn72.opsis.controller.web")
public class VueExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(VueExceptionHandler.class);

    @ExceptionHandler(RessourceIntrouvableException.class)
    public String handleIntrouvable(RessourceIntrouvableException e,
                                    @SessionAttribute(name = UtilisateurConnecte.ATTRIBUT, required = false) UtilisateurConnecte utilisateur,
                                    HttpServletResponse response, Model model) {
        return afficherErreur(HttpServletResponse.SC_NOT_FOUND, "Élément introuvable", e.getMessage(),
                utilisateur, response, model);
    }

    @ExceptionHandler(ModificationIncidentInterditeException.class)
    public String handleModificationInterdite(ModificationIncidentInterditeException e,
                                              @SessionAttribute(name = UtilisateurConnecte.ATTRIBUT, required = false) UtilisateurConnecte utilisateur,
                                              HttpServletResponse response, Model model) {
        return afficherErreur(HttpServletResponse.SC_FORBIDDEN, "Modification impossible", e.getMessage(),
                utilisateur, response, model);
    }

    @ExceptionHandler(AffectationIncidentImpossibleException.class)
    public String handleAffectationImpossible(AffectationIncidentImpossibleException e,
                                              @SessionAttribute(name = UtilisateurConnecte.ATTRIBUT, required = false) UtilisateurConnecte utilisateur,
                                              HttpServletResponse response, Model model) {
        return afficherErreur(HttpServletResponse.SC_CONFLICT, "Affectation impossible", e.getMessage(),
                utilisateur, response, model);
    }

    @ExceptionHandler(EvaluationImpossibleException.class)
    public String handleEvaluationImpossible(EvaluationImpossibleException e,
                                             @SessionAttribute(name = UtilisateurConnecte.ATTRIBUT, required = false) UtilisateurConnecte utilisateur,
                                             HttpServletResponse response, Model model) {
        return afficherErreur(HttpServletResponse.SC_CONFLICT, "Évaluation impossible", e.getMessage(),
                utilisateur, response, model);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public String handleModificationSimultanee(OptimisticLockingFailureException e,
                                               @SessionAttribute(name = UtilisateurConnecte.ATTRIBUT, required = false) UtilisateurConnecte utilisateur,
                                               HttpServletResponse response, Model model) {
        return afficherErreur(HttpServletResponse.SC_CONFLICT, "Modification simultanée",
                "Ces données viennent d'être modifiées par un autre utilisateur : rechargez la page puis recommencez.",
                utilisateur, response, model);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleParametreInvalide(MethodArgumentTypeMismatchException e,
                                          @SessionAttribute(name = UtilisateurConnecte.ATTRIBUT, required = false) UtilisateurConnecte utilisateur,
                                          HttpServletResponse response, Model model) {
        return afficherErreur(HttpServletResponse.SC_BAD_REQUEST, "Adresse invalide",
                "Le paramètre « " + e.getName() + " » n'est pas valide.", utilisateur, response, model);
    }

    @ExceptionHandler(Exception.class)
    public String handleInattendue(Exception e,
                                   @SessionAttribute(name = UtilisateurConnecte.ATTRIBUT, required = false) UtilisateurConnecte utilisateur,
                                   HttpServletResponse response, Model model) {
        log.error("Erreur inattendue sur une vue", e);
        return afficherErreur(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Une erreur est survenue",
                "Un problème technique empêche d'afficher cette page. Réessayez dans quelques instants.",
                utilisateur, response, model);
    }

    @ExceptionHandler(AccesRefuseException.class)
    public String handleAccesRefuse(AccesRefuseException e,
                                    @SessionAttribute(name = UtilisateurConnecte.ATTRIBUT, required = false) UtilisateurConnecte utilisateur,
                                    HttpServletResponse response, Model model) {
        return afficherErreur(HttpServletResponse.SC_FORBIDDEN, "Accès refusé", e.getMessage(),
                utilisateur, response, model);
    }

    private String afficherErreur(int statut, String titre, String message, UtilisateurConnecte utilisateur,
                                  HttpServletResponse response, Model model) {
        response.setStatus(statut);
        model.addAttribute(UtilisateurConnecte.ATTRIBUT, utilisateur);
        model.addAttribute("statut", statut);
        model.addAttribute("titre", titre);
        model.addAttribute("message", message);
        return "erreur";
    }
}
