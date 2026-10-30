package altn72.opsis.dto;

import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.prestataire.Prestataire;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
                versHeureLocale(incident.getDateHeureOuverture()),
                versHeureLocale(incident.getDateHeureResolution()),
                prestataire == null ? null : prestataire.getNom()
        );
    }

    private static LocalDateTime versHeureLocale(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
