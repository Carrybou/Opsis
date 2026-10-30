package altn72.opsis.model.incident;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    @EntityGraph(attributePaths = "idPrestataire")
    List<Incident> findByIdEquipement_IdOrderByDateHeureOuvertureDesc(Long idEquipement);
}
