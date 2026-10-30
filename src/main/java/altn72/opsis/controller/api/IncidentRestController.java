package altn72.opsis.controller.api;

import altn72.opsis.dto.incident.IncidentDetailResponse;
import altn72.opsis.dto.incident.IncidentRequest;
import altn72.opsis.dto.incident.IncidentResponse;
import altn72.opsis.security.UtilisateurConnecte;
import altn72.opsis.service.incident.IncidentInterface;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/incidents")
public class IncidentRestController {

    private final IncidentInterface incidentService;

    public IncidentRestController(IncidentInterface incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    public ResponseEntity<IncidentResponse> declarer(@Valid @RequestBody IncidentRequest request,
                                                     @RequestAttribute(UtilisateurConnecte.ATTRIBUT) UtilisateurConnecte utilisateur) {
        IncidentResponse incident = incidentService.declarer(request, utilisateur.id());
        return ResponseEntity
                .created(URI.create("/api/incidents/" + incident.identifiant()))
                .body(incident);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentDetailResponse> consulter(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.consulterDetail(id));
    }
}