package altn72.opsis.model.utilisateur;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByIdentifiantConnexion(String identifiantConnexion);

    // Les comptes supprimés gardent leur identifiant : il ne peut pas être réattribué.
    boolean existsByIdentifiantConnexion(String identifiantConnexion);

    boolean existsByIdentifiantConnexionAndIdNot(String identifiantConnexion, Long id);

    List<Utilisateur> findByActifTrueOrderByProfilAscPrenomAsc();

    Optional<Utilisateur> findByIdAndActifTrue(Long id);
}
