package altn72.opsis.model.incident;

import java.time.Duration;
import java.time.LocalDateTime;

// RG3 / RG4 : niveau d'attention d'un incident actif, toujours calculé à partir de l'heure courante (jamais stocké en base).
// L'ordre des constantes est celui de l'urgence, utilisé pour trier et grouper le tableau de bord.
public enum NiveauAttention {
    ROUGE("72 h +"),
    ORANGE("48 – 72 h"),
    VERT("< 48 h"),
    AFFECTE("Pris en charge");

    public static final Duration SEUIL_ORANGE = Duration.ofHours(48);
    public static final Duration SEUIL_ROUGE = Duration.ofHours(72);

    private final String libelle;

    NiveauAttention(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    // Un incident affecté ne porte jamais vert / orange / rouge (RG4). Sinon : < 48 h vert, [48 h ; 72 h[ orange, >= 72 h rouge.
    public static NiveauAttention calculer(Incident incident, LocalDateTime maintenant) {
        if (incident.estAffecte()) {
            return AFFECTE;
        }
        return selonAnciennete(Duration.between(incident.getDateHeureOuverture(), maintenant));
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
