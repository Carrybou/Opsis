package altn72.opsis.security;

import altn72.opsis.model.utilisateur.Droit;
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

    public String libelleProfil() {
        return profil.getLibelle();
    }

    public boolean peutDeclarerIncident() {
        return Droit.DECLARER_INCIDENT.estAccordeA(profil);
    }

    public boolean peutResoudreIncident() {
        return Droit.RESOUDRE_INCIDENT.estAccordeA(profil);
    }

    public boolean peutConsulterTableauDeBord() {
        return Droit.CONSULTER_TABLEAU_DE_BORD.estAccordeA(profil);
    }

    public boolean peutEvaluerPrestataire() {
        return Droit.EVALUER_PRESTATAIRE.estAccordeA(profil);
    }

    public boolean peutGererComptes() {
        return Droit.GERER_COMPTES.estAccordeA(profil);
    }
}
