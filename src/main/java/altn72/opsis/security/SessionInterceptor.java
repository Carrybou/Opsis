package altn72.opsis.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class SessionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        UtilisateurConnecte utilisateur = utilisateurEnSession(request);
        if (utilisateur == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (!ControleProfil.estAutorise(handler, utilisateur)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    private UtilisateurConnecte utilisateurEnSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (UtilisateurConnecte) session.getAttribute(UtilisateurConnecte.ATTRIBUT);
    }
}
