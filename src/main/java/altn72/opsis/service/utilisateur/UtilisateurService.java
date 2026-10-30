package altn72.opsis.service.utilisateur;

import altn72.opsis.dto.UtilisateurRequest;
import altn72.opsis.dto.UtilisateurResponse;
import altn72.opsis.exception.IdentifiantDejaUtiliseException;
import altn72.opsis.model.utilisateur.Utilisateur;
import altn72.opsis.model.utilisateur.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UtilisateurService implements UtilisateurInterface {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UtilisateurResponse creer(UtilisateurRequest request) {
        if (utilisateurRepository.existsByIdentifiantConnexion(request.identifiantConnexion())) {
            throw new IdentifiantDejaUtiliseException(request.identifiantConnexion());
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setIdentifiantConnexion(request.identifiantConnexion());
        utilisateur.setMotDePasse(passwordEncoder.encode(request.motDePasse()));
        utilisateur.setPrenom(request.prenom());
        utilisateur.setProfil(request.profil());

        return UtilisateurResponse.from(utilisateurRepository.save(utilisateur));
    }
}