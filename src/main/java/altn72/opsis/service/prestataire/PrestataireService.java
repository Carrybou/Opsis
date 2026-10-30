package altn72.opsis.service.prestataire;

import altn72.opsis.dto.prestataire.PrestataireResponse;
import altn72.opsis.model.prestataire.PrestataireRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PrestataireService implements PrestataireInterface {

    private final PrestataireRepository prestataireRepository;

    public PrestataireService(PrestataireRepository prestataireRepository) {
        this.prestataireRepository = prestataireRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrestataireResponse> lister() {
        return prestataireRepository.findAllByOrderByNomAsc().stream()
                .map(PrestataireResponse::from)
                .toList();
    }
}
