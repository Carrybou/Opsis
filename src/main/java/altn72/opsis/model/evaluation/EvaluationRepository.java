package altn72.opsis.model.evaluation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    Optional<Evaluation> findByIdIncident_Id(Long idIncident);

    boolean existsByIdIncident_Id(Long idIncident);

    List<Evaluation> findByIdIncident_IdPrestataire_Id(Long idPrestataire);

    // Requête SQL native : moyenne et nombre d'évaluations par prestataire, calculés par la base en une seule requête
    // (jointure evaluation -> incident, regroupement par prestataire) plutôt qu'en chargeant toutes les évaluations en Java.
    @Query(value = """
            SELECT i.id_prestataire AS idPrestataire,
                   AVG(e.note)      AS moyenne,
                   COUNT(*)         AS nombre
            FROM evaluation e
            JOIN incident i ON i.identifiant = e.id_incident
            WHERE i.id_prestataire IS NOT NULL
            GROUP BY i.id_prestataire
            """, nativeQuery = true)
    List<NotePrestataire> calculerNotesParPrestataire();
}
