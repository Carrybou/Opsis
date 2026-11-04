package altn72.opsis.dto.utilisateur;

import altn72.opsis.model.utilisateur.UtilisateurType;

import java.util.List;
import java.util.Map;

// Page de gestion des comptes : comptes actifs et synthèse. plusActif est null quand personne n'a encore déclaré d'incident.
public record GestionComptesResponse(
        List<CompteResponse> comptes,
        Map<UtilisateurType, Long> parProfil,
        long declarations30j,
        CompteResponse plusActif
) {
}
