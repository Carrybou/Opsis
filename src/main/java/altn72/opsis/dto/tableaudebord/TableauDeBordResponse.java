package altn72.opsis.dto.tableaudebord;

import altn72.opsis.model.incident.NiveauAttention;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// Vue du tableau de bord (F11). Le service fournit les comptages ; les pourcentages et le tracé SVG sont déduits ici pour que
// le template n'ait qu'à les poser : les graphiques sont rendus côté serveur, sans bibliothèque JavaScript.
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
    // ceux pris en charge sont empilés au-dessus en bleu. La colonne la plus haute occupe toute la hauteur.
    public record TrancheAge(String libelle, NiveauAttention niveauSansPrestataire,
                             long sansPrestataire, long prisEnCharge, long totalMaximal) {
        public long total() {
            return sansPrestataire + prisEnCharge;
        }

        public double hauteurSansPrestataire() {
            return hauteur(sansPrestataire);
        }

        public double hauteurPrisEnCharge() {
            return hauteur(prisEnCharge);
        }

        private double hauteur(long nombre) {
            return nombre * 100.0 / Math.max(1, totalMaximal);
        }
    }

    public record ChargeStation(Long idStation, String nomStation, long total,
                                List<Segment> segments, long totalMaximal) {
        public double largeurTotale() {
            return total * 100.0 / totalMaximal;
        }
    }

    public record Segment(NiveauAttention niveau, long nombre, long totalStation) {
        public double largeur() {
            return nombre * 100.0 / totalStation;
        }
    }

    // Courbe dans un repère 0..100 (x : jours, y : nombre d'incidents actifs ce jour-là, inversé pour le SVG).
    public record Courbe(List<Point> points, long maximum, List<Graduation> graduations) {
        private static final long[] PAS_ROND = {1, 2, 5, 10, 20, 50, 100};
        private static final long PAS_PAR_DEFAUT = 200;
        private static final int INTERVALLES_MAXIMUM = 4;

        public static Courbe de(SortedMap<LocalDate, Long> actifsParJour) {
            long pic = actifsParJour.values().stream().mapToLong(Long::longValue).max().orElse(0);
            long pas = pasDeGraduation(pic);
            long maximum = Math.max(pas, (pic + pas - 1) / pas * pas);

            List<Point> points = new ArrayList<>();
            int indiceDernierJour = actifsParJour.size() - 1;
            for (Map.Entry<LocalDate, Long> jour : actifsParJour.entrySet()) {
                points.add(new Point(jour.getKey(), jour.getValue(),
                        points.size() * 100.0 / indiceDernierJour, 100 - jour.getValue() * 100.0 / maximum));
            }
            List<Graduation> graduations = new ArrayList<>();
            for (long valeur = 0; valeur <= maximum; valeur += pas) {
                graduations.add(new Graduation(valeur, 100 - valeur * 100.0 / maximum));
            }
            return new Courbe(points, maximum, graduations);
        }

        // Pas « rond » pour ne pas dépasser le nombre d'intervalles lisibles sur l'axe.
        private static long pasDeGraduation(long pic) {
            for (long pas : PAS_ROND) {
                if (pic <= pas * INTERVALLES_MAXIMUM) {
                    return pas;
                }
            }
            return PAS_PAR_DEFAUT;
        }

        public String chemin() {
            return points.stream()
                    .map(point -> String.format(Locale.ROOT, "%.2f,%.2f", point.x(), point.y()))
                    .collect(Collectors.joining(" L", "M", ""));
        }

        public String cheminAire() {
            return chemin() + " L100,100 L0,100 Z";
        }

        public Point dernier() {
            return points.getLast();
        }
    }

    public record Point(LocalDate jour, long valeur, double x, double y) {
    }

    public record Graduation(long valeur, double y) {
    }

    public record GroupeIncidents(NiveauAttention niveau, List<IncidentActifResponse> incidents) {
    }
}
