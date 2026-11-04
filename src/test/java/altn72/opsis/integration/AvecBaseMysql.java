package altn72.opsis.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.MountableFile;

// Une vraie base MySQL, créée par bdd.sql comme avec docker-compose : les tests portent sur le schéma réel et ses contraintes
// (ddl-auto=none, Hibernate ne crée rien). Le conteneur est partagé par tous les tests d'intégration et s'arrête avec eux.
public abstract class AvecBaseMysql {

    private static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.0")
            .withDatabaseName("Base_ALTN72")
            .withCopyFileToContainer(MountableFile.forHostPath("bdd.sql"), "/docker-entrypoint-initdb.d/bdd.sql");

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void proprietes(DynamicPropertyRegistry proprietes) {
        proprietes.add("spring.datasource.url", MYSQL::getJdbcUrl);
        proprietes.add("spring.datasource.username", MYSQL::getUsername);
        proprietes.add("spring.datasource.password", MYSQL::getPassword);
        // Clé réservée aux tests : ils ne dépendent ni du .env ni d'aucun secret.
        proprietes.add("jwt.secret", () -> "Y2xlLWRlLXRlc3QtcG91ci1sZXMtdGVzdHMtZC1pbnRlZ3JhdGlvbi1vcHNpcw==");
    }
}
