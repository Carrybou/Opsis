package altn72.opsis.model.utilisateur;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static altn72.opsis.model.utilisateur.UtilisateurType.ADMIN;
import static altn72.opsis.model.utilisateur.UtilisateurType.AGENT;
import static altn72.opsis.model.utilisateur.UtilisateurType.TECHNICIEN;

// Droits de chaque profil, en un seul endroit : UtilisateurConnecte s'en sert pour afficher ou masquer les actions,
// et la page de gestion des comptes pour les présenter. Les routes restent protégées par @ProfilRequis.
public enum Droit {
    CONSULTER("Consulter les stations, l'historique et les prestataires", "Consulter", ADMIN, AGENT, TECHNICIEN),
    DECLARER_INCIDENT("Déclarer un incident", "Déclarer", ADMIN, AGENT),
    MODIFIER_INCIDENT("Modifier un incident", "Modifier", ADMIN, AGENT),
    RESOUDRE_INCIDENT("Déclarer un incident résolu", "Résoudre", ADMIN, TECHNICIEN),
    AFFECTER_PRESTATAIRE("Affecter un prestataire", "Affecter", ADMIN),
    EVALUER_PRESTATAIRE("Évaluer une intervention", "Évaluer", ADMIN),
    CONSULTER_TABLEAU_DE_BORD("Consulter le tableau de bord", "Tableau de bord", ADMIN),
    GERER_COMPTES("Gérer les comptes", "Comptes", ADMIN);

    private final String libelle;
    private final String libelleCourt;
    private final Set<UtilisateurType> profils;

    Droit(String libelle, String libelleCourt, UtilisateurType... profils) {
        this.libelle = libelle;
        this.libelleCourt = libelleCourt;
        this.profils = Set.of(profils);
    }

    public String getLibelle() {
        return libelle;
    }

    public String getLibelleCourt() {
        return libelleCourt;
    }

    public boolean estAccordeA(UtilisateurType profil) {
        return profils.contains(profil);
    }

    public static List<Droit> de(UtilisateurType profil) {
        return Arrays.stream(values()).filter(d -> d.estAccordeA(profil)).toList();
    }
}
