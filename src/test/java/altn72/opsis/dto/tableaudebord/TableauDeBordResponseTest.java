package altn72.opsis.dto.tableaudebord;

import altn72.opsis.dto.tableaudebord.TableauDeBordResponse.Courbe;
import altn72.opsis.dto.tableaudebord.TableauDeBordResponse.TrancheAge;
import altn72.opsis.model.incident.NiveauAttention;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

class TableauDeBordResponseTest {

    @Test
    void courbe_placeLesPointsDansLeRepereEtDonneSonTrace() {
        LocalDate debut = LocalDate.of(2026, 11, 1);
        TreeMap<LocalDate, Long> actifsParJour = new TreeMap<>();
        actifsParJour.put(debut, 0L);
        actifsParJour.put(debut.plusDays(1), 2L);
        actifsParJour.put(debut.plusDays(2), 4L);

        Courbe courbe = Courbe.de(actifsParJour);

        assertThat(courbe.maximum()).isEqualTo(4);
        assertThat(courbe.graduations()).extracting(TableauDeBordResponse.Graduation::valeur).containsExactly(0L, 1L, 2L, 3L, 4L);
        assertThat(courbe.chemin()).isEqualTo("M0.00,100.00 L50.00,50.00 L100.00,0.00");
        assertThat(courbe.cheminAire()).isEqualTo(courbe.chemin() + " L100,100 L0,100 Z");
    }

    @Test
    void trancheAge_sansIncident_garde0DeHauteur() {
        TrancheAge vide = new TrancheAge("< 24 h", NiveauAttention.VERT, 0, 0, 0);
        TrancheAge pleine = new TrancheAge("< 24 h", NiveauAttention.VERT, 3, 1, 4);

        assertThat(List.of(vide.hauteurSansPrestataire(), vide.hauteurPrisEnCharge())).containsOnly(0.0);
        assertThat(pleine.hauteurSansPrestataire()).isEqualTo(75.0);
    }
}
