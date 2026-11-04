package altn72.opsis.dto.incident;

import altn72.opsis.dto.evaluation.EvaluationResponse;
import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.equipement.EquipementType;
import altn72.opsis.model.evaluation.Evaluation;
import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.incident.NiveauAttention;
import altn72.opsis.model.prestataire.Prestataire;
import altn72.opsis.model.station.Station;

import java.time.Duration;
import java.time.LocalDateTime;

public record IncidentDetailResponse(
        Long identifiant,
        String description,
        IncidentStatut statut,
        LocalDateTime dateHeureOuverture,
        LocalDateTime dateHeureResolution,
        boolean resolu,
        String dureeOuverture,
        Long idEquipement,
        String referenceEquipement,
        String libelleEquipement,
        EquipementType typeEquipement,
        Long idStation,
        String nomStation,
        Long idPrestataire,
        String nomPrestataire,
        String contactPrestataire,
        String prenomDeclarant,
        EvaluationResponse evaluation
) {
    public static IncidentDetailResponse from(Incident incident, Evaluation evaluation) {
        Equipement equipement = incident.getEquipement();
        Station station = equipement.getStation();
        Prestataire prestataire = incident.getPrestataire();

        LocalDateTime fin = incident.estResolu() && incident.getDateHeureResolution() != null
                ? incident.getDateHeureResolution()
                : LocalDateTime.now();

        return new IncidentDetailResponse(
                incident.getId(),
                incident.getDescription(),
                incident.getStatut(),
                incident.getDateHeureOuverture(),
                incident.getDateHeureResolution(),
                incident.estResolu(),
                FormatDuree.formater(Duration.between(incident.getDateHeureOuverture(), fin)),
                equipement.getId(),
                equipement.getReferenceUnique(),
                equipement.getLibelleDescription(),
                equipement.getType(),
                station.getId(),
                station.getNom(),
                prestataire != null ? prestataire.getId() : null,
                prestataire != null ? prestataire.getNom() : null,
                prestataire != null ? prestataire.getContact() : null,
                incident.getDeclarant().getPrenom(),
                evaluation != null ? EvaluationResponse.from(evaluation) : null
        );
    }

    // RG3 : recalculé à chaque affichage, comme sur le tableau de bord.
    public NiveauAttention niveauAttention() {
        return NiveauAttention.calculer(statut, dateHeureOuverture, LocalDateTime.now());
    }
}
