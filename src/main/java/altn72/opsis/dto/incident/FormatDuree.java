package altn72.opsis.dto.incident;

import java.time.Duration;

// Durée lisible ("3 j 4 h", "2 h 15 min", "40 min"), partagée par la page détail et le tableau de bord.
public final class FormatDuree {

    private FormatDuree() {
    }

    public static String formater(Duration duree) {
        if (duree.isNegative()) {
            return "0 min";
        }
        long jours = duree.toDays();
        int heures = duree.toHoursPart();
        int minutes = duree.toMinutesPart();

        if (jours > 0) {
            return jours + " j " + heures + " h";
        }
        if (heures > 0) {
            return heures + " h " + minutes + " min";
        }
        return minutes + " min";
    }
}
