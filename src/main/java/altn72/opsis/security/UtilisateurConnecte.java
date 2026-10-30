package altn72.opsis.security;

import altn72.opsis.model.utilisateur.Utilisateur;
import altn72.opsis.model.utilisateur.UtilisateurType;

import java.io.Serializable;

public record UtilisateurConnecte(
        Long id,
        String identifiantConnexion,
        String prenom,
        UtilisateurType profil
) implements Serializable {

    public static final String ATTRIBUT = "utilisateurConnecte";

    public static UtilisateurConnecte from(Utilisateur utilisateur) {
        return new UtilisateurConnecte(
                utilisateur.getId(),
                utilisateur.getIdentifiantConnexion(),
                utilisateur.getPrenom(),
                utilisateur.getProfil()
        );
    }

    public boolean aUnProfil(UtilisateurType... profils) {
        for (UtilisateurType p : profils) {
            if (p == profil) {
                return true;
            }
        }
        return false;
    }
}