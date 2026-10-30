package altn72.opsis.dto;

import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.prestataire.Prestataire;
import altn72.opsis.model.utilisateur.Utilisateur;

import java.time.Instant;

public record IncidentResponse(
        Long identifiant,
        String description,
        IncidentStatut statut,
        Instant dateHeureOuverture,
        Instant dateHeureResolution,
        Long idEquipement,
        String referenceEquipement,
        String libelleEquipement,
        String nomStation,
        Long idPrestataire,
        String nomPrestataire,
        Long idUtilisateur,
        String prenomUtilisateur
) {
    public static IncidentResponse from(Incident incident) {
        Equipement equipement = incident.getIdEquipement();
        Prestataire prestataire = incident.getIdPrestataire();
        Utilisateur utilisateur = incident.getIdUtilisateur();

        return new IncidentResponse(
                incident.getId(),
                incident.getDescription(),
                incident.getStatut(),
                incident.getDateHeureOuverture(),
                incident.getDateHeureResolution(),
                equipement.getId(),
                equipement.getReferenceUnique(),
                equipement.getLibelleDescription(),
                equipement.getIdStation().getNom(),
                prestataire != null ? prestataire.getId() : null,
                prestataire != null ? prestataire.getNom() : null,
                utilisateur.getId(),
                utilisateur.getPrenom()
        );
    }
}