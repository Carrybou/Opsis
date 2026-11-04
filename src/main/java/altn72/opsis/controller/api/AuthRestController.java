package altn72.opsis.controller.api;

import altn72.opsis.dto.login.LoginRequest;
import altn72.opsis.dto.login.LoginResponse;
import altn72.opsis.service.auth.AuthInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentification")
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AuthInterface authService;

    public AuthRestController(AuthInterface authService) {
        this.authService = authService;
    }

    @Operation(summary = "Obtenir un jeton", description = "Seule route de l'API accessible sans jeton.")
    @SecurityRequirements
    @ApiResponse(responseCode = "200", description = "Jeton à envoyer dans l'en-tête `Authorization: Bearer ...`")
    @ApiResponse(responseCode = "400", description = "Corps de requête invalide")
    @ApiResponse(responseCode = "401", description = "Identifiants invalides")
    @PostMapping("/login")
    public LoginResponse connecter(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}