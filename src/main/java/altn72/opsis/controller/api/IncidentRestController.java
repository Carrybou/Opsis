package altn72.opsis.controller.api;

import altn72.opsis.dto.incident.IncidentDetailResponse;
import altn72.opsis.dto.incident.IncidentModificationRequest;
import altn72.opsis.dto.incident.IncidentRequest;
import altn72.opsis.dto.incident.IncidentResponse;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.ProfilRequis;
import altn72.opsis.security.UtilisateurConnecte;
import altn72.opsis.service.incident.IncidentInterface;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/incidents")
public class IncidentRestController {

    private final IncidentInterface incidentService;

    public IncidentRestController(IncidentInterface incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    @ProfilRequis(UtilisateurType.AGENT)
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

    @PatchMapping("/{id}")
    @ProfilRequis({UtilisateurType.ADMIN, UtilisateurType.AGENT})
    public IncidentResponse modifier(@PathVariable Long id,
                                     @Valid @RequestBody IncidentModificationRequest request,
                                     @RequestAttribute(UtilisateurConnecte.ATTRIBUT) UtilisateurConnecte utilisateur) {
        return incidentService.modifier(id, request, utilisateur);
    }
}
