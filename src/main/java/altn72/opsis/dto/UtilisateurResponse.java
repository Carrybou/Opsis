package altn72.opsis.dto;

import altn72.opsis.model.utilisateur.Utilisateur;
import altn72.opsis.model.utilisateur.UtilisateurType;

public record UtilisateurResponse(
        Long identifiant,
        String identifiantConnexion,
        String prenom,
        UtilisateurType profil
) {
    public static UtilisateurResponse from(Utilisateur utilisateur) {
        return new UtilisateurResponse(
                utilisateur.getId(),
                utilisateur.getIdentifiantConnexion(),
                utilisateur.getPrenom(),
                utilisateur.getProfil()
        );
    }
}