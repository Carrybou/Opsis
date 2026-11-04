package altn72.opsis.model.equipement;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipementRepository extends JpaRepository<Equipement, Long> {

    // La station est chargée avec l'équipement : pas de requête supplémentaire par équipement à l'affichage.
    @EntityGraph(attributePaths = "station")
    List<Equipement> findAllByOrderByReferenceUniqueAsc();
}
