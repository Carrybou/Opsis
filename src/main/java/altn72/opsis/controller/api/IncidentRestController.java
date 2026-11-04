package altn72.opsis.controller.api;

import altn72.opsis.dto.evaluation.EvaluationRequest;
import altn72.opsis.dto.evaluation.EvaluationResponse;
import altn72.opsis.dto.incident.AffectationRequest;
import altn72.opsis.dto.incident.IncidentDetailResponse;
import altn72.opsis.dto.incident.IncidentModificationRequest;
import altn72.opsis.dto.incident.IncidentRequest;
import altn72.opsis.dto.incident.IncidentResponse;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.ProfilRequis;
import altn72.opsis.security.UtilisateurConnecte;
import altn72.opsis.service.evaluation.EvaluationInterface;
import altn72.opsis.service.incident.IncidentInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@Tag(name = "Incidents", description = "Cycle de vie d'un incident : déclaration, affectation, résolution, évaluation")
@RestController
@RequestMapping("/api/incidents")
public class IncidentRestController {

    private final IncidentInterface incidentService;
    private final EvaluationInterface evaluationService;

    public IncidentRestController(IncidentInterface incidentService, EvaluationInterface evaluationService) {
        this.incidentService = incidentService;
        this.evaluationService = evaluationService;
    }

    @Operation(summary = "Déclarer un incident", description = "Responsable d'exploitation et agent. L'équipement passe en panne.")
    @ApiResponse(responseCode = "201", description = "Incident créé")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "403", description = "Profil non autorisé")
    @ApiResponse(responseCode = "404", description = "Équipement introuvable")
    @PostMapping
    @ProfilRequis({UtilisateurType.AGENT, UtilisateurType.ADMIN})
    public ResponseEntity<IncidentResponse> declarer(@Valid @RequestBody IncidentRequest request,
                                                     @Parameter(hidden = true) @RequestAttribute(UtilisateurConnecte.ATTRIBUT) UtilisateurConnecte utilisateur) {
        IncidentResponse incident = incidentService.declarer(request, utilisateur.id());
        return ResponseEntity
                .created(URI.create("/api/incidents/" + incident.identifiant()))
                .body(incident);
    }

    @Operation(summary = "Consulter le détail d'un incident")
    @ApiResponse(responseCode = "200", description = "Détail de l'incident")
    @ApiResponse(responseCode = "404", description = "Incident introuvable")
    @GetMapping("/{id}")
    public IncidentDetailResponse consulter(@PathVariable Long id) {
        return incidentService.consulterDetail(id);
    }

    @Operation(summary = "Modifier la description d'un incident",
            description = "Responsable d'exploitation : tout incident non résolu. Agent : ses incidents, tant qu'aucun prestataire n'est affecté.")
    @ApiResponse(responseCode = "200", description = "Incident modifié")
    @ApiResponse(responseCode = "400", description = "Description invalide")
    @ApiResponse(responseCode = "403", description = "Profil non autorisé, ou incident non modifiable (le message donne le motif)")
    @ApiResponse(responseCode = "404", description = "Incident introuvable")
    @ApiResponse(responseCode = "409", description = "Incident modifié entre-temps par un autre utilisateur")
    @PatchMapping("/{id}")
    @ProfilRequis({UtilisateurType.ADMIN, UtilisateurType.AGENT})
    public IncidentResponse modifier(@PathVariable Long id,
                                     @Valid @RequestBody IncidentModificationRequest request,
                                     @Parameter(hidden = true) @RequestAttribute(UtilisateurConnecte.ATTRIBUT) UtilisateurConnecte utilisateur) {
        return incidentService.modifier(id, request, utilisateur);
    }

    @Operation(summary = "Affecter ou réaffecter un prestataire",
            description = "Responsable d'exploitation. L'incident devient affecté et l'équipement passe en maintenance.")
    @ApiResponse(responseCode = "200", description = "Prestataire affecté")
    @ApiResponse(responseCode = "400", description = "Prestataire manquant")
    @ApiResponse(responseCode = "403", description = "Profil non autorisé")
    @ApiResponse(responseCode = "404", description = "Incident ou prestataire introuvable")
    @ApiResponse(responseCode = "409", description = "Incident résolu, prestataire déjà affecté, ou incident modifié entre-temps")
    @PutMapping("/{id}/prestataire")
    @ProfilRequis(UtilisateurType.ADMIN)
    public IncidentResponse affecter(@PathVariable Long id,
                                     @Valid @RequestBody AffectationRequest request,
                                     @Parameter(hidden = true) @RequestAttribute(UtilisateurConnecte.ATTRIBUT) UtilisateurConnecte utilisateur) {
        return incidentService.affecter(id, request, utilisateur);
    }

    @Operation(summary = "Déclarer un incident résolu",
            description = "Responsable d'exploitation et technicien. L'état de l'équipement est recalculé selon ses incidents restants.")
    @ApiResponse(responseCode = "200", description = "Incident résolu")
    @ApiResponse(responseCode = "403", description = "Profil non autorisé")
    @ApiResponse(responseCode = "404", description = "Incident introuvable")
    @ApiResponse(responseCode = "409", description = "Incident déjà résolu, ou modifié entre-temps")
    @ProfilRequis({UtilisateurType.ADMIN, UtilisateurType.TECHNICIEN})
    @PostMapping("/{id}/resolution")
    public IncidentDetailResponse resoudre(@PathVariable Long id,
                                           @Parameter(hidden = true) @RequestAttribute(UtilisateurConnecte.ATTRIBUT) UtilisateurConnecte utilisateur) {
        return incidentService.resoudre(id, utilisateur);
    }

    @Operation(summary = "Consulter l'historique des incidents résolus", description = "Du plus récent au plus ancien, par date de résolution.")
    @ApiResponse(responseCode = "200", description = "Incidents résolus")
    @GetMapping("/historique")
    public List<IncidentResponse> historique() {
        return incidentService.listerHistoriqueResolus();
    }

    @Operation(summary = "Évaluer l'intervention sur un incident",
            description = "Responsable d'exploitation. Note entière de 1 à 5, une seule fois, sur un incident résolu et affecté à un prestataire.")
    @ApiResponse(responseCode = "201", description = "Évaluation enregistrée")
    @ApiResponse(responseCode = "400", description = "Note invalide")
    @ApiResponse(responseCode = "403", description = "Profil non autorisé")
    @ApiResponse(responseCode = "404", description = "Incident introuvable")
    @ApiResponse(responseCode = "409", description = "Incident non résolu, sans prestataire, ou déjà évalué")
    @PostMapping("/{id}/evaluation")
    @ProfilRequis(UtilisateurType.ADMIN)
    public ResponseEntity<EvaluationResponse> evaluer(@PathVariable Long id,
                                                      @Valid @RequestBody EvaluationRequest request,
                                                      @Parameter(hidden = true) @RequestAttribute(UtilisateurConnecte.ATTRIBUT) UtilisateurConnecte utilisateur) {
        EvaluationResponse evaluation = evaluationService.evaluer(id, request, utilisateur);
        return ResponseEntity
                .created(URI.create("/api/incidents/" + id))
                .body(evaluation);
    }
}
