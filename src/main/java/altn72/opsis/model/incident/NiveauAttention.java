package altn72.opsis.model.incident;

import java.time.Duration;
import java.time.LocalDateTime;

// RG3 / RG4 : niveau d'attention d'un incident actif, toujours calculé à partir de l'heure courante (jamais stocké en base).
// L'ordre des constantes est celui de l'urgence, utilisé pour trier et grouper le tableau de bord.
public enum NiveauAttention {
    ROUGE(NiveauAttention.HEURES_SEUIL_ROUGE + " h +"),
    ORANGE(NiveauAttention.HEURES_SEUIL_ORANGE + " – " + NiveauAttention.HEURES_SEUIL_ROUGE + " h"),
    VERT("< " + NiveauAttention.HEURES_SEUIL_ORANGE + " h"),
    AFFECTE("Pris en charge");

    // RG3 : les seuils sont définis ici et nulle part ailleurs ; libellés, graphiques et pages en sont déduits.
    public static final int HEURES_SEUIL_ORANGE = 48;
    public static final int HEURES_SEUIL_ROUGE = 72;
    public static final Duration SEUIL_ORANGE = Duration.ofHours(HEURES_SEUIL_ORANGE);
    public static final Duration SEUIL_ROUGE = Duration.ofHours(HEURES_SEUIL_ROUGE);

    private final String libelle;

    NiveauAttention(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    // Un incident affecté ne porte jamais vert / orange / rouge (RG4). Sinon : < 48 h vert, [48 h ; 72 h[ orange, >= 72 h rouge.
    public static NiveauAttention calculer(Incident incident, LocalDateTime maintenant) {
        return calculer(incident.getStatut(), incident.getDateHeureOuverture(), maintenant);
    }

    public static NiveauAttention calculer(IncidentStatut statut, LocalDateTime ouverture, LocalDateTime maintenant) {
        if (statut == IncidentStatut.EN_COURS) {
            return AFFECTE;
        }
        return selonAnciennete(Duration.between(ouverture, maintenant));
    }

    public static NiveauAttention selonAnciennete(Duration ouvert) {
        if (ouvert.compareTo(SEUIL_ROUGE) >= 0) {
            return ROUGE;
        }
        if (ouvert.compareTo(SEUIL_ORANGE) >= 0) {
            return ORANGE;
        }
        return VERT;
    }
}
