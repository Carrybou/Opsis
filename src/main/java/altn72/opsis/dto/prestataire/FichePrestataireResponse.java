package altn72.opsis.dto.prestataire;

import java.util.List;

public record FichePrestataireResponse(
        PrestataireResponse prestataire,
        List<InterventionResponse> interventionsEnCours,
        List<InterventionResponse> interventionsTerminees
) {
}
