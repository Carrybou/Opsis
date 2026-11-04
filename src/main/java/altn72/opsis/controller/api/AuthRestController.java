package altn72.opsis.controller.api;

import altn72.opsis.dto.login.LoginRequest;
import altn72.opsis.dto.login.LoginResponse;
import altn72.opsis.service.auth.AuthInterface;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AuthInterface authService;

    public AuthRestController(AuthInterface authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse connecter(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}