package altn72.opsis.dto.incident;

import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.incident.NiveauAttention;
import altn72.opsis.model.prestataire.Prestataire;

import java.time.LocalDateTime;

public record IncidentResume(
        Long identifiant,
        String description,
        IncidentStatut statut,
        LocalDateTime dateHeureOuverture,
        LocalDateTime dateHeureResolution,
        String nomPrestataire,
        NiveauAttention niveauAttention
) {
    // RG3 : le niveau est recalculé à chaque consultation, jamais stocké.
    public static IncidentResume from(Incident incident, LocalDateTime maintenant) {
        Prestataire prestataire = incident.getPrestataire();
        return new IncidentResume(
                incident.getId(),
                incident.getDescription(),
                incident.getStatut(),
                incident.getDateHeureOuverture(),
                incident.getDateHeureResolution(),
                prestataire == null ? null : prestataire.getNom(),
                NiveauAttention.calculer(incident, maintenant)
        );
    }
}
