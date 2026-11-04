package altn72.opsis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

// Heure courante injectée : les niveaux d'attention (RG3) se calculent sur l'horloge système, et sur une horloge fixe dans les tests.
@Configuration
public class HorlogeConfig {

    @Bean
    public Clock horloge() {
        return Clock.systemDefaultZone();
    }
}
