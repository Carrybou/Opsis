package altn72.opsis.dto.utilisateur;

import altn72.opsis.model.utilisateur.UtilisateurType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Formulaire de création et de modification d'un compte (page de gestion des comptes).
// Le mot de passe est obligatoire à la création ; en modification, le laisser vide conserve l'actuel.
public class CompteRequest {

    @NotBlank(message = "L'identifiant de connexion est obligatoire")
    @Size(min = 3, max = 100, message = "L'identifiant doit faire entre 3 et 100 caractères")
    @Pattern(regexp = "^[a-zA-Z0-9._-]*$", message = "L'identifiant ne peut contenir que des lettres, chiffres, points, tirets et underscores")
    private String identifiantConnexion;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
    private String prenom;

    @NotNull(message = "Veuillez choisir un profil")
    private UtilisateurType profil;

    @Pattern(regexp = "^$|^.{8,72}$", message = "Le mot de passe doit faire entre 8 et 72 caractères")
    private String motDePasse = "";

    public CompteRequest() {
    }

    public CompteRequest(String identifiantConnexion, String prenom, UtilisateurType profil) {
        this.identifiantConnexion = identifiantConnexion;
        this.prenom = prenom;
        this.profil = profil;
    }

    public boolean aUnMotDePasse() {
        return motDePasse != null && !motDePasse.isEmpty();
    }

    public String getIdentifiantConnexion() {
        return identifiantConnexion;
    }

    public void setIdentifiantConnexion(String identifiantConnexion) {
        this.identifiantConnexion = identifiantConnexion;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public UtilisateurType getProfil() {
        return profil;
    }

    public void setProfil(UtilisateurType profil) {
        this.profil = profil;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }
}
