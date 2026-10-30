package altn72.opsis.model.evaluation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    Optional<Evaluation> findByIdIncident_Id(Long idIncident);

}
