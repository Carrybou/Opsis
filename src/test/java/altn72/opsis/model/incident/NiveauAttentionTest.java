package altn72.opsis.model.incident;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NiveauAttentionTest {

    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 11, 4, 12, 0);

    // RG3 : bornes exactes, « un incident ouvert depuis 47 h est vert ; à 48 h pile, il devient orange ».
    @Test
    void calculer_incidentNonAffecte_appliqueLesSeuilsDe48hEt72h() {
        assertThat(niveau(IncidentStatut.OUVERT, MAINTENANT.minusHours(47))).isEqualTo(NiveauAttention.VERT);
        assertThat(niveau(IncidentStatut.OUVERT, MAINTENANT.minusHours(48).plusSeconds(1))).isEqualTo(NiveauAttention.VERT);
        assertThat(niveau(IncidentStatut.OUVERT, MAINTENANT.minusHours(48))).isEqualTo(NiveauAttention.ORANGE);
        assertThat(niveau(IncidentStatut.OUVERT, MAINTENANT.minusHours(72).plusSeconds(1))).isEqualTo(NiveauAttention.ORANGE);
        assertThat(niveau(IncidentStatut.OUVERT, MAINTENANT.minusHours(72))).isEqualTo(NiveauAttention.ROUGE);
    }

    // RG4 : un incident affecté ne porte jamais vert / orange / rouge, même ouvert depuis longtemps.
    @Test
    void calculer_incidentAffecte_estPrisEnChargeQuelleQueSoitSonAnciennete() {
        assertThat(niveau(IncidentStatut.EN_COURS, MAINTENANT.minusDays(10))).isEqualTo(NiveauAttention.AFFECTE);
    }

    private static NiveauAttention niveau(IncidentStatut statut, LocalDateTime ouverture) {
        Incident incident = new Incident();
        incident.setStatut(statut);
        incident.setDateHeureOuverture(ouverture);
        return NiveauAttention.calculer(incident, MAINTENANT);
    }
}
