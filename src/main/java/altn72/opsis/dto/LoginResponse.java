package altn72.opsis.dto;

import altn72.opsis.model.utilisateur.UtilisateurType;

import java.time.Instant;

public record LoginResponse(
        String token,
        String type,
        Instant expiration,
        String prenom,
        UtilisateurType profil
) {}