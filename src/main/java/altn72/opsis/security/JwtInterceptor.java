package altn72.opsis.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final String PREFIXE = "Bearer ";

    private final JwtService jwtService;

    public JwtInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(PREFIXE)) {
            return refuser(response, HttpServletResponse.SC_UNAUTHORIZED, "Token manquant");
        }

        Optional<UtilisateurConnecte> utilisateur = jwtService.extraireUtilisateur(header.substring(PREFIXE.length()));
        if (utilisateur.isEmpty()) {
            return refuser(response, HttpServletResponse.SC_UNAUTHORIZED, "Token invalide ou expiré");
        }

        request.setAttribute(UtilisateurConnecte.ATTRIBUT, utilisateur.get());

        ProfilRequis profilRequis = trouverAnnotation(handler);
        if (profilRequis != null && !utilisateur.get().aUnProfil(profilRequis.value())) {
            return refuser(response, HttpServletResponse.SC_FORBIDDEN, "Accès refusé pour ce profil");
        }

        return true;
    }

    private ProfilRequis trouverAnnotation(Object handler) {
        if (!(handler instanceof HandlerMethod methode)) {
            return null;
        }
        ProfilRequis surMethode = methode.getMethodAnnotation(ProfilRequis.class);
        if (surMethode != null) {
            return surMethode;
        }
        return methode.getBeanType().getAnnotation(ProfilRequis.class);
    }

    private boolean refuser(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"message\":\"" + message + "\"}");
        return false;
    }
}