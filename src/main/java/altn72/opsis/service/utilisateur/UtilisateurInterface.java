package altn72.opsis.service.utilisateur;

import altn72.opsis.dto.utilisateur.CompteRequest;
import altn72.opsis.dto.utilisateur.GestionComptesResponse;
import altn72.opsis.dto.utilisateur.UtilisateurRequest;
import altn72.opsis.dto.utilisateur.UtilisateurResponse;
import altn72.opsis.security.UtilisateurConnecte;

public interface UtilisateurInterface {

    UtilisateurResponse creer(UtilisateurRequest request);

    GestionComptesResponse consulterComptes();

    UtilisateurResponse modifier(Long id, CompteRequest request, UtilisateurConnecte auteur);

    void supprimer(Long id, UtilisateurConnecte auteur);
}
