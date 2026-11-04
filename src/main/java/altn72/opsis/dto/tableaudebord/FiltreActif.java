package altn72.opsis.dto.tableaudebord;

// Étiquette d'un filtre appliqué, avec l'URL du tableau de bord sans ce filtre (bouton ✕).
public record FiltreActif(String libelle, String urlSansCeFiltre) {
}
