package altn72.opsis.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

// §7.4 : ces erreurs naissent avant les contrôleurs, là où les advices ne passent pas. MockMvc ne les reproduit pas : il faut un vrai serveur.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ErreursHttpIT extends AvecBaseMysql {

    private final HttpClient client = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Test
    void apiAdresseInconnue_renvoie404EnFrancaisAuFormatCommun() throws Exception {
        HttpResponse<String> reponse = client.send(requete("/api/inconnue").header("Authorization", "Bearer " + token()).GET().build(), HttpResponse.BodyHandlers.ofString());

        assertThat(reponse.statusCode()).isEqualTo(404);
        assertThat((String) JsonPath.read(reponse.body(), "$.message")).isEqualTo("La ressource demandée est introuvable");
    }

    @Test
    void apiMethodeNonPriseEnCharge_renvoie405SansDetailTechnique() throws Exception {
        HttpResponse<String> reponse = client.send(requete("/api/incidents/1").header("Authorization", "Bearer " + token()).DELETE().build(), HttpResponse.BodyHandlers.ofString());

        assertThat(reponse.statusCode()).isEqualTo(405);
        assertThat(reponse.body()).contains("n'est pas possible").doesNotContain("DELETE", "supported");
    }

    @Test
    void pageMethodeNonPriseEnCharge_afficheLaPageDuSite() throws Exception {
        HttpResponse<String> reponse = client.send(requete("/login").header("Accept", "text/html").DELETE().build(), HttpResponse.BodyHandlers.ofString());

        assertThat(reponse.statusCode()).isEqualTo(405);
        assertThat(reponse.body()).contains("Action impossible").doesNotContain("Whitelabel", "DELETE");
    }

    private String token() throws Exception {
        HttpResponse<String> reponse = client.send(requete("/api/auth/login")
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString("{\"identifiantConnexion\":\"agent.test\",\"motDePasse\":\"1234\"}"))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        return JsonPath.read(reponse.body(), "$.token");
    }

    private HttpRequest.Builder requete(String chemin) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + chemin));
    }
}
