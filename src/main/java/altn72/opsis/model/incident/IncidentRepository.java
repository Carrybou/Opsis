package altn72.opsis.model.incident;

import altn72.opsis.model.evaluation.Evaluation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    @EntityGraph(attributePaths = "idPrestataire")
    List<Incident> findByIdEquipement_IdOrderByDateHeureOuvertureDesc(Long idEquipement);

    boolean existsByIdEquipement_IdAndStatutNotAndIdNot(Long idEquipement, IncidentStatut statut, Long idIncident);

    boolean existsByIdEquipement_IdAndStatutAndIdNot(Long idEquipement, IncidentStatut statut, Long idIncident);

    @EntityGraph(attributePaths = {"idEquipement", "idEquipement.idStation", "idPrestataire", "idUtilisateur"})
    List<Incident> findByStatutOrderByDateHeureResolutionDesc(IncidentStatut statut);

    List<Incident> findByIdPrestataire_IdOrderByDateHeureOuvertureDesc(Long idPrestataire);

    List<Incident> findByStatutNotOrderByDateHeureOuvertureDesc(IncidentStatut statut);
}
