package altn72.opsis.model.prestataire;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrestataireRepository extends JpaRepository<Prestataire, Long> {

    List<Prestataire> findAllByOrderByNomAsc();
}
