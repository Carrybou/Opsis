package altn72.opsis.model.utilisateur;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByIdentifiantConnexion(String identifiantConnexion);

    boolean existsByIdentifiantConnexion(String identifiantConnexion);
}