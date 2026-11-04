package altn72.opsis.dto.carte;

import altn72.opsis.model.equipement.EquipementEtat;
import altn72.opsis.model.equipement.EquipementType;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.incident.NiveauAttention;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Lot alpha, cartographie : une station, l'état de ses équipements et ce qu'on peut ouvrir depuis son panneau.
public record CarteReseauResponse(
        List<StationCarte> stations,
        long nombreEnPanne,
        long nombreEnMaintenance,
        long nombreFonctionnelles,
        long nombreSansEquipement
) {
    // situation : l'état le plus grave parmi les équipements (couleur du marqueur), null si la station n'en a aucun.
    // Il décrit les équipements, pas la station : une station reste ouverte même si un équipement est en panne.
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

        public String resumeEquipements() {
            int total = equipements.size();
            if (total == 0) {
                return "Aucun équipement";
            }
            String nombre = total == 1 ? "1 équipement" : total + " équipements";
            if (nombreEnPanne == 0 && nombreEnMaintenance == 0) {
                return nombre + (total == 1 ? " · fonctionnel" : " · tous fonctionnels");
            }
            String detail = nombre;
            if (nombreEnPanne > 0) {
                detail += " · " + nombreEnPanne + " en panne";
            }
            if (nombreEnMaintenance > 0) {
                detail += " · " + nombreEnMaintenance + " en maintenance";
            }
            return detail;
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
            IncidentStatut statut,
            LocalDateTime dateHeureOuverture
    ) {
        // RG3 : recalculé à chaque affichage, comme sur le tableau de bord.
        public NiveauAttention niveauAttention() {
            return NiveauAttention.calculer(statut, dateHeureOuverture, LocalDateTime.now());
        }
    }
}
