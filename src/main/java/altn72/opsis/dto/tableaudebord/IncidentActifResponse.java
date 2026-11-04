package altn72.opsis.dto.tableaudebord;

import altn72.opsis.dto.incident.FormatDuree;
import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.equipement.EquipementType;
import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.NiveauAttention;
import altn72.opsis.model.prestataire.Prestataire;
import altn72.opsis.model.station.Station;

import java.time.Duration;
import java.time.LocalDateTime;

// echeance / progression : où en est un incident non affecté par rapport au seuil suivant de RG3
// (« orange dans 4 h », barre remplie à 92 %), ou de combien il a dépassé le rouge. Null / 0 pour un incident pris en charge.
public record IncidentActifResponse(
        Long identifiant,
        String description,
        NiveauAttention niveau,
        LocalDateTime dateHeureOuverture,
        String dureeOuverture,
        String echeance,
        int progression,
        Long idEquipement,
        String referenceEquipement,
        EquipementType typeEquipement,
        Long idStation,
        String nomStation,
        Long idPrestataire,
        String nomPrestataire
) {
    public static IncidentActifResponse from(Incident incident, LocalDateTime maintenant) {
        Equipement equipement = incident.getIdEquipement();
        Station station = equipement.getIdStation();
        Prestataire prestataire = incident.getIdPrestataire();
        Duration ouvert = Duration.between(incident.getDateHeureOuverture(), maintenant);
        NiveauAttention niveau = NiveauAttention.calculer(incident, maintenant);

        Avancement avancement = Avancement.de(niveau, ouvert);

        return new IncidentActifResponse(
                incident.getId(),
                incident.getDescription(),
                niveau,
                incident.getDateHeureOuverture(),
                FormatDuree.formater(ouvert),
                avancement.echeance(),
                avancement.progression(),
                equipement.getId(),
                equipement.getReferenceUnique(),
                equipement.getType(),
                station.getId(),
                station.getNom(),
                prestataire != null ? prestataire.getId() : null,
                prestataire != null ? prestataire.getNom() : null
        );
    }

    public boolean estSansPrestataire() {
        return niveau != NiveauAttention.AFFECTE;
    }

    private record Avancement(String echeance, int progression) {
        private static final Avancement AUCUN = new Avancement(null, 0);

        static Avancement de(NiveauAttention niveau, Duration ouvert) {
            return switch (niveau) {
                case VERT -> new Avancement("orange dans " + FormatDuree.formater(NiveauAttention.SEUIL_ORANGE.minus(ouvert)),
                        pourcentage(ouvert, NiveauAttention.SEUIL_ORANGE));
                case ORANGE -> new Avancement("rouge dans " + FormatDuree.formater(NiveauAttention.SEUIL_ROUGE.minus(ouvert)),
                        pourcentage(ouvert.minus(NiveauAttention.SEUIL_ORANGE), NiveauAttention.SEUIL_ROUGE.minus(NiveauAttention.SEUIL_ORANGE)));
                case ROUGE -> new Avancement("en retard de " + FormatDuree.formater(ouvert.minus(NiveauAttention.SEUIL_ROUGE)), 100);
                case AFFECTE -> AUCUN;
            };
        }
    }

    private static int pourcentage(Duration ecoule, Duration total) {
        return (int) Math.max(0, Math.min(100, ecoule.toMinutes() * 100 / total.toMinutes()));
    }
}
