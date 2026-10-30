package altn72.opsis.service.utilisateur;

import altn72.opsis.dto.UtilisateurRequest;
import altn72.opsis.dto.UtilisateurResponse;

public interface UtilisateurInterface {

    UtilisateurResponse creer(UtilisateurRequest request);
}