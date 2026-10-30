package altn72.opsis.security;

import org.springframework.web.method.HandlerMethod;

public final class ControleProfil {

    private ControleProfil() {
    }

    public static boolean estAutorise(Object handler, UtilisateurConnecte utilisateur) {
        ProfilRequis profilRequis = trouverAnnotation(handler);
        return profilRequis == null || utilisateur.aUnProfil(profilRequis.value());
    }

    private static ProfilRequis trouverAnnotation(Object handler) {
        if (!(handler instanceof HandlerMethod methode)) {
            return null;
        }
        ProfilRequis surMethode = methode.getMethodAnnotation(ProfilRequis.class);
        if (surMethode != null) {
            return surMethode;
        }
        return methode.getBeanType().getAnnotation(ProfilRequis.class);
    }
}
