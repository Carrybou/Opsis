package altn72.opsis.dto.tableaudebord;

import altn72.opsis.model.incident.NiveauAttention;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// Vue du tableau de bord (F11). Les pourcentages (largeurs, hauteurs, positions) sont calculés ici pour que le template
// n'ait qu'à les poser : les graphiques sont rendus côté serveur, en HTML / SVG, sans bibliothèque JavaScript.
public record TableauDeBordResponse(
        LocalDateTime genereA,
        Indicateurs indicateurs,
        Map<NiveauAttention, Long> compteParNiveau,
        List<TrancheAge> tranchesAge,
        List<ChargeStation> chargeParStation,
        Courbe courbeActifs,
        List<GroupeIncidents> groupes,
        int nombreAffiches,
        Map<Long, String> stations
) {

    // variationActifs7j est null quand un filtre de niveau est appliqué : le niveau dépend de l'heure, il n'a pas d'historique.
    public record Indicateurs(long actifs, Long variationActifs7j,
                              long sansPrestataire, long rougesSansPrestataire,
                              long equipementsHorsService, long equipementsEnMaintenance, long equipementsTotal,
                              long resolus7j, long resolus7jPrecedents) {

        public int pourcentageHorsService() {
            return equipementsTotal == 0 ? 0 : (int) Math.round(equipementsHorsService * 100.0 / equipementsTotal);
        }

        public long variationResolus() {
            return resolus7j - resolus7jPrecedents;
        }
    }

    // Une colonne de l'histogramme d'âge : les incidents sans prestataire prennent la couleur RG3 de la tranche,
    // ceux pris en charge sont empilés au-dessus en bleu.
    public record TrancheAge(String libelle, NiveauAttention niveauSansPrestataire,
                             long sansPrestataire, long prisEnCharge,
                             double hauteurSansPrestataire, double hauteurPrisEnCharge) {
        public long total() {
            return sansPrestataire + prisEnCharge;
        }
    }

    public record ChargeStation(Long idStation, String nomStation, long total,
                                List<Segment> segments, double largeurTotale) {
    }

    public record Segment(NiveauAttention niveau, long nombre, double largeur) {
    }

    // Courbe dans un repère 0..100 (x : jours, y : nombre d'incidents actifs ce jour-là, inversé pour le SVG).
    public record Courbe(List<Point> points, long maximum, List<Graduation> graduations, String chemin, String cheminAire) {
        public Point dernier() {
            return points.get(points.size() - 1);
        }
    }

    public record Point(LocalDate jour, long valeur, double x, double y) {
    }

    public record Graduation(long valeur, double y) {
    }

    public record GroupeIncidents(NiveauAttention niveau, List<IncidentActifResponse> incidents) {
    }
}
