package altn72.opsis.service.prestataire;

import altn72.opsis.dto.prestataire.FichePrestataireResponse;
import altn72.opsis.dto.prestataire.InterventionResponse;
import altn72.opsis.dto.prestataire.PrestataireResponse;
import altn72.opsis.exception.PrestataireIntrouvableException;
import altn72.opsis.model.evaluation.Evaluation;
import altn72.opsis.model.evaluation.EvaluationRepository;
import altn72.opsis.model.evaluation.NotePrestataire;
import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentRepository;
import altn72.opsis.model.prestataire.Prestataire;
import altn72.opsis.model.prestataire.PrestataireRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PrestataireService implements PrestataireInterface {

    private final PrestataireRepository prestataireRepository;
    private final EvaluationRepository evaluationRepository;
    private final IncidentRepository incidentRepository;

    public PrestataireService(PrestataireRepository prestataireRepository, EvaluationRepository evaluationRepository,
                              IncidentRepository incidentRepository) {
        this.prestataireRepository = prestataireRepository;
        this.evaluationRepository = evaluationRepository;
        this.incidentRepository = incidentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrestataireResponse> lister() {
        Map<Long, NotePrestataire> notes = notesParPrestataire();
        return prestataireRepository.findAllByOrderByNomAsc().stream()
                .map(prestataire -> PrestataireResponse.from(prestataire, notes.get(prestataire.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PrestataireResponse trouver(Long idPrestataire) {
        Prestataire prestataire = prestataireRepository.findById(idPrestataire)
                .orElseThrow(() -> new PrestataireIntrouvableException(idPrestataire));
        return PrestataireResponse.from(prestataire, notesParPrestataire().get(idPrestataire));
    }

    // Interventions en cours par date d'ouverture, terminées par date de résolution (comme l'historique, RG7), du plus récent au plus ancien.
    @Override
    @Transactional(readOnly = true)
    public FichePrestataireResponse consulterFiche(Long idPrestataire) {
        PrestataireResponse prestataire = trouver(idPrestataire);
        Map<Long, Integer> notes = evaluationRepository.findByIdIncident_IdPrestataire_Id(idPrestataire).stream()
                .collect(Collectors.toMap(evaluation -> evaluation.getIdIncident().getId(), Evaluation::getNote));

        List<Incident> incidents = incidentRepository.findByIdPrestataire_IdOrderByDateHeureOuvertureDesc(idPrestataire);
        List<InterventionResponse> enCours = incidents.stream()
                .filter(incident -> !incident.estResolu())
                .map(incident -> InterventionResponse.from(incident, null))
                .toList();
        List<InterventionResponse> terminees = incidents.stream()
                .filter(Incident::estResolu)
                .sorted(Comparator.comparing(Incident::getDateHeureResolution, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(incident -> InterventionResponse.from(incident, notes.get(incident.getId())))
                .toList();

        return new FichePrestataireResponse(prestataire, enCours, terminees);
    }

    private Map<Long, NotePrestataire> notesParPrestataire() {
        return evaluationRepository.calculerNotesParPrestataire().stream()
                .collect(Collectors.toMap(NotePrestataire::getIdPrestataire, Function.identity()));
    }
}
