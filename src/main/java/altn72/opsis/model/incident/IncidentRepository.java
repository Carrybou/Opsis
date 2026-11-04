package altn72.opsis.model.incident;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    @EntityGraph(attributePaths = "idPrestataire")
    List<Incident> findByIdEquipement_IdOrderByDateHeureOuvertureDesc(Long idEquipement);

    boolean existsByIdEquipement_IdAndStatutNotAndIdNot(Long idEquipement, IncidentStatut statut, Long idIncident);

    boolean existsByIdEquipement_IdAndStatutAndIdNot(Long idEquipement, IncidentStatut statut, Long idIncident);

    @EntityGraph(attributePaths = {"idEquipement", "idEquipement.idStation", "idPrestataire", "idUtilisateur"})
    List<Incident> findByStatutOrderByDateHeureResolutionDesc(IncidentStatut statut);

    List<Incident> findByIdPrestataire_IdOrderByDateHeureOuvertureDesc(Long idPrestataire);

    List<Incident> findByStatutNotOrderByDateHeureOuvertureDesc(IncidentStatut statut);

    @Query("""
            select i.idUtilisateur.id as idUtilisateur, count(i) as declares,
                   sum(case when i.statut <> altn72.opsis.model.incident.IncidentStatut.RESOLU then 1 else 0 end) as actifs,
                   max(i.dateHeureOuverture) as derniereDeclaration
            from Incident i
            group by i.idUtilisateur.id""")
    List<DeclarationsUtilisateur> compterDeclarationsParUtilisateur();

    long countByDateHeureOuvertureGreaterThanEqual(LocalDateTime depuis);
}
