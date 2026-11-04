package altn72.opsis.integration;

import altn72.opsis.model.equipement.EquipementEtat;
import altn72.opsis.model.equipement.EquipementRepository;
import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentRepository;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.UtilisateurConnecte;
import altn72.opsis.service.incident.IncidentInterface;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// RG10 : deux utilisateurs travaillent sur le même incident. Le premier l'a lu, le second l'a modifié entre-temps :
// le premier doit échouer entièrement, et ne laisser aucune de ses modifications (incident comme équipement).
// L'incident 16 (équipement 7, en panne) ne sert pas aux autres tests d'intégration.
@SpringBootTest
class EcrituresSimultaneesIT extends AvecBaseMysql {

    private static final long INCIDENT = 16L;
    private static final long EQUIPEMENT = 7L;
    private static final UtilisateurConnecte TECHNICIEN =
            new UtilisateurConnecte(9L, "technicien.test", "Test", UtilisateurType.TECHNICIEN);

    @Autowired
    private IncidentInterface incidentService;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private EquipementRepository equipementRepository;

    @Autowired
    private PlatformTransactionManager transactions;

    @Test
    void uneResolutionConcurrenteDUneAutreEcriture_echoueSansRienEnregistrer() {
        TransactionTemplate premier = new TransactionTemplate(transactions);
        TransactionTemplate second = new TransactionTemplate(transactions);
        second.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        assertThatThrownBy(() -> premier.executeWithoutResult(etat -> {
            incidentRepository.findById(INCIDENT).orElseThrow();
            second.executeWithoutResult(autre -> incidentRepository.findById(INCIDENT).orElseThrow()
                    .setDescription("Description modifiée par un autre utilisateur"));
            incidentService.resoudre(INCIDENT, TECHNICIEN);
        })).isInstanceOf(OptimisticLockingFailureException.class);

        Incident incident = incidentRepository.findById(INCIDENT).orElseThrow();
        assertThat(incident.getDescription()).isEqualTo("Description modifiée par un autre utilisateur");
        assertThat(incident.getStatut()).isEqualTo(IncidentStatut.OUVERT);
        assertThat(incident.getDateHeureResolution()).isNull();
        assertThat(equipementRepository.findById(EQUIPEMENT).orElseThrow().getEtatFonctionnement()).isEqualTo(EquipementEtat.EN_PANNE);
    }
}
