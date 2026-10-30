package altn72.opsis.service.auth;

import altn72.opsis.exception.IdentifiantsInvalidesException;
import altn72.opsis.model.utilisateur.Utilisateur;
import altn72.opsis.model.utilisateur.UtilisateurRepository;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.JwtService;
import altn72.opsis.security.UtilisateurConnecte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String IDENTIFIANT = "thomas";
    private static final String MOT_DE_PASSE = "1234";

    // Un vrai encodeur BCrypt : le test vérifie ainsi que le mot de passe est comparé à son hash, jamais en clair.
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void initialiser() {
        authService = new AuthService(utilisateurRepository, passwordEncoder, jwtService);
    }

    @Test
    void authentifier_identifiantsValides_renvoieLUtilisateurConnecte() {
        when(utilisateurRepository.findByIdentifiantConnexion(IDENTIFIANT)).thenReturn(Optional.of(utilisateur()));

        UtilisateurConnecte connecte = authService.authentifier(IDENTIFIANT, MOT_DE_PASSE);

        assertThat(connecte.identifiantConnexion()).isEqualTo(IDENTIFIANT);
        assertThat(connecte.prenom()).isEqualTo("Thomas");
        assertThat(connecte.profil()).isEqualTo(UtilisateurType.ADMIN);
    }

    @Test
    void authentifier_motDePasseIncorrect_leveIdentifiantsInvalidesException() {
        when(utilisateurRepository.findByIdentifiantConnexion(IDENTIFIANT)).thenReturn(Optional.of(utilisateur()));

        assertThatThrownBy(() -> authService.authentifier(IDENTIFIANT, "mauvais"))
                .isInstanceOf(IdentifiantsInvalidesException.class)
                .hasMessage("Identifiant ou mot de passe incorrect");
    }

    @Test
    void authentifier_identifiantInconnu_leveLeMemeMessageQuUnMotDePasseIncorrect() {
        when(utilisateurRepository.findByIdentifiantConnexion("inconnu")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authentifier("inconnu", MOT_DE_PASSE))
                .isInstanceOf(IdentifiantsInvalidesException.class)
                .hasMessage("Identifiant ou mot de passe incorrect");
    }

    private Utilisateur utilisateur() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(7L);
        utilisateur.setIdentifiantConnexion(IDENTIFIANT);
        utilisateur.setMotDePasse(passwordEncoder.encode(MOT_DE_PASSE));
        utilisateur.setPrenom("Thomas");
        utilisateur.setProfil(UtilisateurType.ADMIN);
        return utilisateur;
    }
}
