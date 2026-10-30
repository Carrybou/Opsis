package altn72.opsis.security;

import altn72.opsis.model.utilisateur.Utilisateur;
import altn72.opsis.model.utilisateur.UtilisateurType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final SecretKey cle;
    private final Duration expiration;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration}") Duration expiration) {
        this.cle = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiration = expiration;
    }

    public String generer(Utilisateur utilisateur) {
        Instant maintenant = Instant.now();
        return Jwts.builder()
                .subject(utilisateur.getIdentifiantConnexion())
                .claim("id", utilisateur.getId())
                .claim("prenom", utilisateur.getPrenom())
                .claim("profil", utilisateur.getProfil().name())
                .issuedAt(Date.from(maintenant))
                .expiration(Date.from(maintenant.plus(expiration)))
                .signWith(cle)
                .compact();
    }

    public Optional<Claims> valider(String token) {
        try {
            return Optional.of(Jwts.parser()
                    .verifyWith(cle)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public Instant dateExpiration(String token) {
        return valider(token).map(claims -> claims.getExpiration().toInstant()).orElseThrow();
    }

    public Optional<UtilisateurConnecte> extraireUtilisateur(String token) {
        return valider(token).map(claims -> new UtilisateurConnecte(
                claims.get("id", Long.class),
                claims.getSubject(),
                claims.get("prenom", String.class),
                UtilisateurType.valueOf(claims.get("profil", String.class))
        ));
    }
}