package altn72.opsis.exception;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Réponse JSON des erreurs survenues avant d'atteindre un contrôleur (adresse inconnue, méthode non prise en charge) :
// les advices ne les voient pas. Même format que ApiError, sans le texte technique de Spring.
@Component
public class AttributsErreurHttp extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest request, ErrorAttributeOptions options) {
        Object status = super.getErrorAttributes(request, options).get("status");
        int code = status instanceof Integer valeur ? valeur : 500;

        Map<String, Object> attributs = new LinkedHashMap<>();
        attributs.put("status", code);
        attributs.put("message", MessageErreurHttp.pour(code));
        attributs.put("erreurs", Map.of());
        attributs.put("date", LocalDateTime.now());
        return attributs;
    }
}
