package altn72.opsis.model.incident;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    @EntityGraph(attributePaths = "prestataire")
    List<Incident> findByEquipement_IdOrderByDateHeureOuvertureDesc(Long idEquipement);

    boolean existsByEquipement_IdAndStatut(Long idEquipement, IncidentStatut statut);

    boolean existsByEquipement_IdAndStatutNotAndIdNot(Long idEquipement, IncidentStatut statut, Long idIncident);

    boolean existsByEquipement_IdAndStatutAndIdNot(Long idEquipement, IncidentStatut statut, Long idIncident);

    @EntityGraph(attributePaths = {"equipement", "equipement.station", "prestataire", "declarant"})
    List<Incident> findByStatutOrderByDateHeureResolutionDesc(IncidentStatut statut);

    List<Incident> findByPrestataire_IdOrderByDateHeureOuvertureDesc(Long idPrestataire);

    @EntityGraph(attributePaths = {"equipement", "equipement.station", "prestataire"})
    List<Incident> findByStatutNotOrderByDateHeureOuvertureDesc(IncidentStatut statut);

    @Query("""
            select i.declarant.id as idUtilisateur, count(i) as declares,
                   sum(case when i.statut <> altn72.opsis.model.incident.IncidentStatut.RESOLU then 1 else 0 end) as actifs,
                   max(i.dateHeureOuverture) as derniereDeclaration
            from Incident i
            group by i.declarant.id""")
    List<DeclarationsUtilisateur> compterDeclarationsParUtilisateur();

    long countByDateHeureOuvertureGreaterThanEqual(LocalDateTime depuis);
}
