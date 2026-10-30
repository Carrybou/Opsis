package altn72.opsis.service.auth;

import altn72.opsis.dto.LoginRequest;
import altn72.opsis.dto.LoginResponse;
import altn72.opsis.exception.IdentifiantsInvalidesException;
import altn72.opsis.model.utilisateur.Utilisateur;
import altn72.opsis.model.utilisateur.UtilisateurRepository;
import altn72.opsis.security.JwtService;
import altn72.opsis.security.UtilisateurConnecte;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements AuthInterface {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UtilisateurRepository utilisateurRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Utilisateur utilisateur = verifierIdentifiants(request.identifiantConnexion(), request.motDePasse());
        String token = jwtService.generer(utilisateur);
        return new LoginResponse(
                token,
                "Bearer",
                jwtService.dateExpiration(token),
                utilisateur.getPrenom(),
                utilisateur.getProfil()
        );
    }

    @Override
    public UtilisateurConnecte authentifier(String identifiantConnexion, String motDePasse) {
        return UtilisateurConnecte.from(verifierIdentifiants(identifiantConnexion, motDePasse));
    }

    private Utilisateur verifierIdentifiants(String identifiantConnexion, String motDePasse) {
        return utilisateurRepository.findByIdentifiantConnexion(identifiantConnexion)
                .filter(u -> passwordEncoder.matches(motDePasse, u.getMotDePasse()))
                .orElseThrow(IdentifiantsInvalidesException::new);
    }
}