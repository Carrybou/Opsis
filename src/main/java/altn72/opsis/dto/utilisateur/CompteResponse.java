package altn72.opsis.dto.utilisateur;

import altn72.opsis.model.incident.DeclarationsUtilisateur;
import altn72.opsis.model.utilisateur.Droit;
import altn72.opsis.model.utilisateur.Utilisateur;
import altn72.opsis.model.utilisateur.UtilisateurType;

import java.time.LocalDateTime;
import java.util.List;

// Une ligne de la page de gestion des comptes : le compte, ses droits et son activité de déclaration.
public record CompteResponse(
        Long identifiant,
        String identifiantConnexion,
        String prenom,
        UtilisateurType profil,
        List<Droit> droits,
        long incidentsDeclares,
        long incidentsActifs,
        LocalDateTime derniereDeclaration
) {
    public static CompteResponse from(Utilisateur utilisateur, DeclarationsUtilisateur declarations) {
        return new CompteResponse(
                utilisateur.getId(),
                utilisateur.getIdentifiantConnexion(),
                utilisateur.getPrenom(),
                utilisateur.getProfil(),
                Droit.de(utilisateur.getProfil()),
                declarations == null ? 0 : declarations.getDeclares().longValue(),
                declarations == null ? 0 : declarations.getActifs().longValue(),
                declarations == null ? null : declarations.getDerniereDeclaration()
        );
    }

    // Tous les profils peuvent consulter : on ne liste que ce qui les distingue.
    public List<Droit> droitsParticuliers() {
        return droits.stream().filter(d -> d != Droit.CONSULTER).toList();
    }

    public boolean aTousLesDroits() {
        return droits.size() == Droit.values().length;
    }
}
