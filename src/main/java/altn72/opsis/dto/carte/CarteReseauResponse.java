package altn72.opsis.dto.carte;

import altn72.opsis.model.equipement.EquipementEtat;
import altn72.opsis.model.equipement.EquipementType;
import altn72.opsis.model.incident.IncidentStatut;

import java.math.BigDecimal;
import java.util.List;

// Lot alpha, cartographie : une station, sa situation et ce qu'on peut ouvrir depuis son panneau.
public record CarteReseauResponse(
        List<StationCarte> stations,
        long nombreEnPanne,
        long nombreEnMaintenance,
        long nombreFonctionnelles,
        long nombreSansEquipement
) {
    // situation : l'état le plus grave parmi les équipements, null si la station n'en a aucun.
    public record StationCarte(
            Long identifiant,
            String nom,
            BigDecimal latitude,
            BigDecimal longitude,
            EquipementEtat situation,
            List<EquipementCarte> equipements,
            long nombreEnPanne,
            long nombreEnMaintenance
    ) {
        public boolean estLocalisee() {
            return latitude != null && longitude != null;
        }
    }

    public record EquipementCarte(
            Long identifiant,
            String reference,
            String libelle,
            EquipementType type,
            EquipementEtat etat,
            List<IncidentCarte> incidentsActifs
    ) {
    }

    public record IncidentCarte(
            Long identifiant,
            String description,
            IncidentStatut statut
    ) {
    }
}
