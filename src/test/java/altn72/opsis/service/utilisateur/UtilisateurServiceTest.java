package altn72.opsis.service.utilisateur;

import altn72.opsis.dto.utilisateur.CompteRequest;
import altn72.opsis.exception.OperationCompteInterditeException;
import altn72.opsis.model.incident.IncidentRepository;
import altn72.opsis.model.utilisateur.Utilisateur;
import altn72.opsis.model.utilisateur.UtilisateurRepository;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.UtilisateurConnecte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    private static final UtilisateurConnecte RESPONSABLE =
            new UtilisateurConnecte(1L, "admin", "Roméo", UtilisateurType.ADMIN);

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UtilisateurService utilisateurService;

    @BeforeEach
    void initialiser() {
        utilisateurService = new UtilisateurService(utilisateurRepository, incidentRepository, passwordEncoder,
                Clock.systemDefaultZone());
    }

    @Test
    void supprimer_desactiveLeCompteSansToucherASesIncidents_maisJamaisLeSien() {
        Utilisateur agent = utilisateur(4L, UtilisateurType.AGENT);
        when(utilisateurRepository.findByIdAndActifTrue(4L)).thenReturn(Optional.of(agent));

        utilisateurService.supprimer(4L, RESPONSABLE);

        assertThat(agent.isActif()).isFalse();
        verifyNoInteractions(incidentRepository);
        assertThatThrownBy(() -> utilisateurService.supprimer(1L, RESPONSABLE))
                .isInstanceOf(OperationCompteInterditeException.class);
    }

    @Test
    void modifier_motDePasseVideConserveLActuel_etSonPropreProfilNePeutPasChanger() {
        Utilisateur agent = utilisateur(4L, UtilisateurType.AGENT);
        when(utilisateurRepository.findByIdAndActifTrue(4L)).thenReturn(Optional.of(agent));

        utilisateurService.modifier(4L, new CompteRequest("hugo.b", "Hugo", UtilisateurType.TECHNICIEN), RESPONSABLE);

        assertThat(agent.getIdentifiantConnexion()).isEqualTo("hugo.b");
        assertThat(agent.getProfil()).isEqualTo(UtilisateurType.TECHNICIEN);
        assertThat(agent.getMotDePasse()).isEqualTo("hash-actuel");

        when(utilisateurRepository.findByIdAndActifTrue(1L)).thenReturn(Optional.of(utilisateur(1L, UtilisateurType.ADMIN)));
        assertThatThrownBy(() -> utilisateurService.modifier(1L,
                new CompteRequest("admin", "Roméo", UtilisateurType.AGENT), RESPONSABLE))
                .isInstanceOf(OperationCompteInterditeException.class);
    }

    private static Utilisateur utilisateur(Long id, UtilisateurType profil) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(id);
        utilisateur.setIdentifiantConnexion("compte" + id);
        utilisateur.setPrenom("Prénom");
        utilisateur.setMotDePasse("hash-actuel");
        utilisateur.setProfil(profil);
        return utilisateur;
    }
}
