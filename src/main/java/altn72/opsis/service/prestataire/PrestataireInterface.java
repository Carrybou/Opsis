package altn72.opsis.service.prestataire;

import altn72.opsis.dto.prestataire.FichePrestataireResponse;
import altn72.opsis.dto.prestataire.PrestataireResponse;

import java.util.List;

public interface PrestataireInterface {

    List<PrestataireResponse> lister();

    PrestataireResponse trouver(Long idPrestataire);

    FichePrestataireResponse consulterFiche(Long idPrestataire);
}
