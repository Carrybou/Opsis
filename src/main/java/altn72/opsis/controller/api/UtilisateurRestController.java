package altn72.opsis.controller.api;

import altn72.opsis.dto.utilisateur.UtilisateurRequest;
import altn72.opsis.dto.utilisateur.UtilisateurResponse;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.ProfilRequis;
import altn72.opsis.service.utilisateur.UtilisateurInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Tag(name = "Utilisateurs")
@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurRestController {

    private final UtilisateurInterface utilisateurService;

    public UtilisateurRestController(UtilisateurInterface utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    // Seul un responsable peut créer un compte : sans cette règle, n'importe quel profil connecté pouvait se créer un compte responsable.
    @Operation(summary = "Créer un compte", description = "Responsable d'exploitation.")
    @ApiResponse(responseCode = "201", description = "Compte créé")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "403", description = "Profil non autorisé")
    @ApiResponse(responseCode = "409", description = "Identifiant déjà utilisé")
    @PostMapping
    @ProfilRequis(UtilisateurType.ADMIN)
    public ResponseEntity<UtilisateurResponse> creer(@Valid @RequestBody UtilisateurRequest request) {
        UtilisateurResponse cree = utilisateurService.creer(request);
        return ResponseEntity
                .created(URI.create("/api/utilisateurs/" + cree.identifiant()))
                .body(cree);
    }
}
