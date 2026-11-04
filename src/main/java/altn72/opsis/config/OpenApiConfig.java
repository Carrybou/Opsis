package altn72.opsis.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// F12 : documentation de l'API, consultable sur /swagger-ui.html une fois connecté au site.
@Configuration
public class OpenApiConfig {

    private static final String SCHEMA_JWT = "jeton";

    @Bean
    public OpenAPI documentationOpsis() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Opsis")
                        .version("1.0")
                        .description("""
                                Suivi des incidents sur les équipements des stations.

                                Obtenez un jeton avec `POST /api/auth/login`, puis renseignez-le avec le bouton **Authorize**.
                                Les erreurs sont renvoyées au même format : `status`, `message` en français, `erreurs` par champ et `date`."""))
                .components(new Components().addSecuritySchemes(SCHEMA_JWT, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(SCHEMA_JWT));
    }
}
