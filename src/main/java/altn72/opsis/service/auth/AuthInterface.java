package altn72.opsis.service.auth;

import altn72.opsis.dto.login.LoginRequest;
import altn72.opsis.dto.login.LoginResponse;
import altn72.opsis.security.UtilisateurConnecte;

public interface AuthInterface {

    LoginResponse login(LoginRequest request);

    UtilisateurConnecte authentifier(String identifiantConnexion, String motDePasse);
}