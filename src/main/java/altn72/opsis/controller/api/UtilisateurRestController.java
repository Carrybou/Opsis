package altn72.opsis.controller.api;

import altn72.opsis.dto.UtilisateurRequest;
import altn72.opsis.dto.UtilisateurResponse;
import altn72.opsis.exception.IdentifiantDejaUtiliseException;
import altn72.opsis.service.utilisateur.UtilisateurInterface;
import org.springframework.http.HttpStatus;
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

    @PostMapping
    public ResponseEntity<UtilisateurResponse> postUtilisateur(@RequestBody UtilisateurRequest request) {
        UtilisateurResponse cree = utilisateurService.creer(request);
        return ResponseEntity
                .created(URI.create("/api/utilisateurs/" + cree.identifiant()))
                .body(cree);
    }

    @ExceptionHandler(IdentifiantDejaUtiliseException.class)
    public ResponseEntity<String> handleIdentifiantDejaUtilise(IdentifiantDejaUtiliseException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}