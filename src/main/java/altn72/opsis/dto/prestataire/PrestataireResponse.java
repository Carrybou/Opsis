package altn72.opsis.dto.prestataire;

import altn72.opsis.model.prestataire.Prestataire;

public record PrestataireResponse(
        Long identifiant,
        String nom,
        String contact
) {
    public static PrestataireResponse from(Prestataire prestataire) {
        return new PrestataireResponse(prestataire.getId(), prestataire.getNom(), prestataire.getContact());
    }
}
