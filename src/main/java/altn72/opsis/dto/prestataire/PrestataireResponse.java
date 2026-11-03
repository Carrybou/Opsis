package altn72.opsis.dto.prestataire;

import altn72.opsis.model.evaluation.NotePrestataire;
import altn72.opsis.model.prestataire.Prestataire;

// noteMoyenne est null tant que le prestataire n'a reçu aucune évaluation (F10).
public record PrestataireResponse(
        Long identifiant,
        String nom,
        String contact,
        Double noteMoyenne,
        long nombreEvaluations
) {
    public static PrestataireResponse from(Prestataire prestataire, NotePrestataire note) {
        return new PrestataireResponse(
                prestataire.getId(),
                prestataire.getNom(),
                prestataire.getContact(),
                note != null ? note.getMoyenne().doubleValue() : null,
                note != null ? note.getNombre().longValue() : 0
        );
    }

    public boolean estEvalue() {
        return nombreEvaluations > 0;
    }
}
