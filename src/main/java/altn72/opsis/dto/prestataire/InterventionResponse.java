package altn72.opsis.dto.prestataire;

import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentStatut;

import java.time.LocalDateTime;

// Un incident vu depuis la fiche du prestataire qui y est (ou y a été) affecté ; note est null si l'intervention n'est pas évaluée.
public record InterventionResponse(
        Long identifiant,
        String description,
        IncidentStatut statut,
        LocalDateTime dateHeureOuverture,
        LocalDateTime dateHeureResolution,
        Long idEquipement,
        String referenceEquipement,
        String nomStation,
        Integer note
) {
    public static InterventionResponse from(Incident incident, Integer note) {
        Equipement equipement = incident.getIdEquipement();
        return new InterventionResponse(
                incident.getId(),
                incident.getDescription(),
                incident.getStatut(),
                incident.getDateHeureOuverture(),
                incident.getDateHeureResolution(),
                equipement.getId(),
                equipement.getReferenceUnique(),
                equipement.getIdStation().getNom(),
                note
        );
    }
}
