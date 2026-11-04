package altn72.opsis.service.utilisateur;

import altn72.opsis.dto.utilisateur.CompteRequest;
import altn72.opsis.dto.utilisateur.CompteResponse;
import altn72.opsis.dto.utilisateur.GestionComptesResponse;
import altn72.opsis.dto.utilisateur.UtilisateurRequest;
import altn72.opsis.dto.utilisateur.UtilisateurResponse;
import altn72.opsis.exception.IdentifiantDejaUtiliseException;
import altn72.opsis.exception.OperationCompteInterditeException;
import altn72.opsis.exception.UtilisateurIntrouvableException;
import altn72.opsis.model.incident.DeclarationsUtilisateur;
import altn72.opsis.model.incident.IncidentRepository;
import altn72.opsis.model.utilisateur.Utilisateur;
import altn72.opsis.model.utilisateur.UtilisateurRepository;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.UtilisateurConnecte;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UtilisateurService implements UtilisateurInterface {

    private final UtilisateurRepository utilisateurRepository;
    private final IncidentRepository incidentRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock horloge;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, IncidentRepository incidentRepository,
                              PasswordEncoder passwordEncoder, Clock horloge) {
        this.utilisateurRepository = utilisateurRepository;
        this.incidentRepository = incidentRepository;
        this.passwordEncoder = passwordEncoder;
        this.horloge = horloge;
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

    @Override
    @Transactional(readOnly = true)
    public GestionComptesResponse consulterComptes() {
        Map<Long, DeclarationsUtilisateur> declarations = incidentRepository.compterDeclarationsParUtilisateur().stream()
                .collect(Collectors.toMap(DeclarationsUtilisateur::getIdUtilisateur, Function.identity()));
        List<CompteResponse> comptes = utilisateurRepository.findByActifTrueOrderByProfilAscPrenomAsc().stream()
                .map(u -> CompteResponse.from(u, declarations.get(u.getId())))
                .toList();

        Map<UtilisateurType, Long> parProfil = new EnumMap<>(UtilisateurType.class);
        for (UtilisateurType profil : UtilisateurType.values()) {
            parProfil.put(profil, comptes.stream().filter(c -> c.profil() == profil).count());
        }
        CompteResponse plusActif = comptes.stream()
                .filter(c -> c.incidentsDeclares() > 0)
                .max(Comparator.comparingLong(CompteResponse::incidentsDeclares))
                .orElse(null);
        long declarations30j = incidentRepository.countByDateHeureOuvertureGreaterThanEqual(
                LocalDateTime.now(horloge).minusDays(30));

        return new GestionComptesResponse(comptes, parProfil, declarations30j, plusActif);
    }

    @Override
    @Transactional
    public UtilisateurResponse modifier(Long id, CompteRequest request, UtilisateurConnecte auteur) {
        Utilisateur utilisateur = trouverActif(id);
        if (id.equals(auteur.id()) && request.getProfil() != utilisateur.getProfil()) {
            throw new OperationCompteInterditeException("Vous ne pouvez pas changer votre propre profil");
        }
        if (utilisateurRepository.existsByIdentifiantConnexionAndIdNot(request.getIdentifiantConnexion(), id)) {
            throw new IdentifiantDejaUtiliseException(request.getIdentifiantConnexion());
        }

        utilisateur.setIdentifiantConnexion(request.getIdentifiantConnexion());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setProfil(request.getProfil());
        if (request.aUnMotDePasse()) {
            utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        }
        return UtilisateurResponse.from(utilisateur);
    }

    @Override
    @Transactional
    public void supprimer(Long id, UtilisateurConnecte auteur) {
        if (id.equals(auteur.id())) {
            throw new OperationCompteInterditeException("Vous ne pouvez pas supprimer votre propre compte");
        }
        trouverActif(id).setActif(false);
    }

    private Utilisateur trouverActif(Long id) {
        return utilisateurRepository.findByIdAndActifTrue(id)
                .orElseThrow(() -> new UtilisateurIntrouvableException(id));
    }
}
