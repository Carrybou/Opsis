package altn72.opsis.controller.api;

import altn72.opsis.dto.utilisateur.UtilisateurRequest;
import altn72.opsis.dto.utilisateur.UtilisateurResponse;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.ProfilRequis;
import altn72.opsis.service.utilisateur.UtilisateurInterface;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurRestController {

    private final UtilisateurInterface utilisateurService;

    public UtilisateurRestController(UtilisateurInterface utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    // Seul un responsable peut créer un compte : sans cette règle, n'importe quel profil connecté pouvait se créer un compte responsable.
    @PostMapping
    @ProfilRequis(UtilisateurType.ADMIN)
    public ResponseEntity<UtilisateurResponse> creer(@Valid @RequestBody UtilisateurRequest request) {
        UtilisateurResponse cree = utilisateurService.creer(request);
        return ResponseEntity
                .created(URI.create("/api/utilisateurs/" + cree.identifiant()))
                .body(cree);
    }
}
