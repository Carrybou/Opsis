package altn72.opsis.dto.tableaudebord;

import altn72.opsis.model.equipement.EquipementType;
import altn72.opsis.model.incident.NiveauAttention;

import java.util.Set;

// Critères optionnels du tableau de bord (null ou vide = pas de filtre). Ils s'appliquent aux indicateurs, aux graphiques et à la liste.
// tri : ordre des incidents dans chaque groupe d'urgence ("anciennete" par défaut, "recent" ou "station").
public record FiltresTableauDeBord(
        Long idStation,
        Set<NiveauAttention> niveaux,
        EquipementType type,
        String recherche,
        String tri
) {
    public static final String TRI_ANCIENNETE = "anciennete";
    public static final String TRI_RECENT = "recent";
    public static final String TRI_STATION = "station";

    public FiltresTableauDeBord {
        niveaux = niveaux == null ? Set.of() : Set.copyOf(niveaux);
        recherche = recherche == null || recherche.isBlank() ? null : recherche.trim();
        tri = tri == null ? TRI_ANCIENNETE : tri;
    }

    public static FiltresTableauDeBord aucun() {
        return new FiltresTableauDeBord(null, null, null, null, null);
    }

    public boolean estFiltre() {
        return idStation != null || !niveaux.isEmpty() || type != null || recherche != null;
    }

    public boolean contientNiveau(NiveauAttention niveau) {
        return niveaux.contains(niveau);
    }
}
