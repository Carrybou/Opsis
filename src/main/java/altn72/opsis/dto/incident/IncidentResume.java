package altn72.opsis.dto.incident;

import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.prestataire.Prestataire;

import java.time.LocalDateTime;

public record IncidentResume(
        Long identifiant,
        String description,
        IncidentStatut statut,
        LocalDateTime dateHeureOuverture,
        LocalDateTime dateHeureResolution,
        String nomPrestataire
) {
    public static IncidentResume from(Incident incident) {
        Prestataire prestataire = incident.getIdPrestataire();
        return new IncidentResume(
                incident.getId(),
                incident.getDescription(),
                incident.getStatut(),
                incident.getDateHeureOuverture(),
                incident.getDateHeureResolution(),
                prestataire == null ? null : prestataire.getNom()
        );
    }
}
