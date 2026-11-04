package altn72.opsis.controller.web;

import altn72.opsis.dto.utilisateur.CompteRequest;
import altn72.opsis.dto.utilisateur.CompteResponse;
import altn72.opsis.dto.utilisateur.UtilisateurRequest;
import altn72.opsis.dto.utilisateur.UtilisateurResponse;
import altn72.opsis.exception.acces.OperationCompteInterditeException;
import altn72.opsis.exception.conflit.IdentifiantDejaUtiliseException;
import altn72.opsis.model.utilisateur.Droit;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.ProfilRequis;
import altn72.opsis.security.UtilisateurConnecte;
import altn72.opsis.service.utilisateur.UtilisateurInterface;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Gestion des comptes, réservée au responsable d'exploitation. Création et modification se font sur la page même :
// ?creer=true ouvre le formulaire de création, ?modifier={id} passe la ligne du compte en édition.
@Controller
@ProfilRequis(UtilisateurType.ADMIN)
public class CompteViewController {

    private final UtilisateurInterface utilisateurService;

    public CompteViewController(UtilisateurInterface utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping("/comptes")
    public String lister(@RequestParam(defaultValue = "false") boolean creer,
                         @RequestParam(required = false) Long modifier,
                         Model model) {
        preparerPage(creer, modifier, model);
        return "comptes";
    }

    @PostMapping("/comptes")
    public String creer(@Valid @ModelAttribute("nouveauCompte") CompteRequest request,
                        BindingResult resultat,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (!request.aUnMotDePasse()) {
            resultat.rejectValue("motDePasse", "obligatoire", "Le mot de passe est obligatoire");
        }
        if (!resultat.hasErrors()) {
            try {
                UtilisateurResponse cree = utilisateurService.creer(new UtilisateurRequest(
                        request.getIdentifiantConnexion(), request.getMotDePasse(), request.getPrenom(), request.getProfil()));
                redirectAttributes.addFlashAttribute("succes", "Le compte " + cree.identifiantConnexion() + " a bien été créé.");
                return "redirect:/comptes";
            } catch (IdentifiantDejaUtiliseException e) {
                resultat.rejectValue("identifiantConnexion", "dejaUtilise", e.getMessage());
            }
        }
        preparerPage(true, null, model);
        return "comptes";
    }

    @PutMapping("/comptes/{id}")
    public String modifier(@PathVariable Long id,
                           @Valid @ModelAttribute("compteModifie") CompteRequest request,
                           BindingResult resultat,
                           @SessionAttribute(UtilisateurConnecte.ATTRIBUT) UtilisateurConnecte utilisateur,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (!resultat.hasErrors()) {
            try {
                UtilisateurResponse modifie = utilisateurService.modifier(id, request, utilisateur);
                if (id.equals(utilisateur.id())) {
                    // L'en-tête lit le prénom dans la session : on la met à jour quand on modifie son propre compte.
                    session.setAttribute(UtilisateurConnecte.ATTRIBUT, new UtilisateurConnecte(
                            modifie.identifiant(), modifie.identifiantConnexion(), modifie.prenom(), modifie.profil()));
                }
                redirectAttributes.addFlashAttribute("succes", "Le compte " + modifie.identifiantConnexion() + " a bien été modifié.");
                return "redirect:/comptes";
            } catch (IdentifiantDejaUtiliseException e) {
                resultat.rejectValue("identifiantConnexion", "dejaUtilise", e.getMessage());
            } catch (OperationCompteInterditeException e) {
                redirectAttributes.addFlashAttribute("erreur", e.getMessage());
                return "redirect:/comptes";
            }
        }
        preparerPage(false, id, model);
        return "comptes";
    }

    @DeleteMapping("/comptes/{id}")
    public String supprimer(@PathVariable Long id,
                            @SessionAttribute(UtilisateurConnecte.ATTRIBUT) UtilisateurConnecte utilisateur,
                            RedirectAttributes redirectAttributes) {
        try {
            utilisateurService.supprimer(id, utilisateur);
            redirectAttributes.addFlashAttribute("succes", "Le compte a été supprimé.");
        } catch (OperationCompteInterditeException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/comptes";
    }

    // Les formulaires déjà présents dans le modèle (saisie invalide renvoyée par @ModelAttribute) sont conservés.
    private void preparerPage(boolean enCreation, Long idEnModification, Model model) {
        var gestion = utilisateurService.consulterComptes();
        model.addAttribute("gestion", gestion);
        model.addAttribute("profils", UtilisateurType.values());
        model.addAttribute("droits", Droit.values());
        model.addAttribute("enCreation", enCreation);
        if (!model.containsAttribute("nouveauCompte")) {
            model.addAttribute("nouveauCompte", new CompteRequest());
        }

        CompteResponse enModification = gestion.comptes().stream()
                .filter(c -> c.identifiant().equals(idEnModification))
                .findFirst().orElse(null);
        model.addAttribute("idEnModification", enModification == null ? null : enModification.identifiant());
        if (!model.containsAttribute("compteModifie")) {
            model.addAttribute("compteModifie", enModification == null ? new CompteRequest()
                    : new CompteRequest(enModification.identifiantConnexion(), enModification.prenom(), enModification.profil()));
        }
    }
}
