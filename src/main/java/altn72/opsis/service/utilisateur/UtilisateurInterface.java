package altn72.opsis.service.utilisateur;

import altn72.opsis.dto.utilisateur.UtilisateurRequest;
import altn72.opsis.dto.utilisateur.UtilisateurResponse;

public interface UtilisateurInterface {

    UtilisateurResponse creer(UtilisateurRequest request);
}