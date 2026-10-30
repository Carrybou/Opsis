package altn72.opsis.dto;

import altn72.opsis.model.utilisateur.UtilisateurType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UtilisateurRequest(

        @NotBlank(message = "L'identifiant de connexion est obligatoire")
        @Size(min = 3, max = 100, message = "L'identifiant doit faire entre 3 et 100 caractères")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "L'identifiant ne peut contenir que des lettres, chiffres, points, tirets et underscores")
        String identifiantConnexion,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 72, message = "Le mot de passe doit faire entre 8 et 72 caractères")
        String motDePasse,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
        String prenom,

        @NotNull(message = "Le profil est obligatoire")
        UtilisateurType profil
) {}