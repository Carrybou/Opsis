package altn72.opsis.service.incident;

import altn72.opsis.dto.incident.AffectationRequest;
import altn72.opsis.dto.incident.IncidentDetailResponse;
import altn72.opsis.dto.incident.IncidentModificationRequest;
import altn72.opsis.dto.incident.IncidentRequest;
import altn72.opsis.dto.incident.IncidentResponse;
import altn72.opsis.exception.AffectationIncidentImpossibleException;
import altn72.opsis.exception.IncidentDejaResoluException;
import altn72.opsis.exception.ModificationIncidentInterditeException;
import altn72.opsis.exception.PrestataireIntrouvableException;
import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.equipement.EquipementEtat;
import altn72.opsis.model.equipement.EquipementRepository;
import altn72.opsis.model.evaluation.EvaluationRepository;
import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentRepository;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.prestataire.Prestataire;
import altn72.opsis.model.prestataire.PrestataireRepository;
import altn72.opsis.model.station.Station;
import altn72.opsis.model.utilisateur.Utilisateur;
import altn72.opsis.model.utilisateur.UtilisateurRepository;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.UtilisateurConnecte;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    private static final Long ID_AGENT_AUTEUR = 4L;
    private static final UtilisateurConnecte RESPONSABLE =
            new UtilisateurConnecte(1L, "admin", "Roméo", UtilisateurType.ADMIN);
    private static final UtilisateurConnecte AGENT_AUTEUR =
            new UtilisateurConnecte(ID_AGENT_AUTEUR, "agent.bernard", "Hugo", UtilisateurType.AGENT);
    private static final UtilisateurConnecte AUTRE_AGENT =
            new UtilisateurConnecte(5L, "agent.petit", "Léa", UtilisateurType.AGENT);
    private static final UtilisateurConnecte TECHNICIEN =
            new UtilisateurConnecte(2L, "tech.martin", "Lucas", UtilisateurType.TECHNICIEN);
    private static final IncidentModificationRequest NOUVELLE_DESCRIPTION =
            new IncidentModificationRequest("  Ascenseur bloqué au deuxième niveau  ");

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private EquipementRepository equipementRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PrestataireRepository prestataireRepository;

    @Mock
    private EvaluationRepository evaluationRepository;

    @Spy
    private Clock horloge = Clock.fixed(Instant.parse("2026-11-04T11:00:00Z"), ZoneId.of("Europe/Paris"));

    @InjectMocks
    private IncidentService incidentService;

    // RG1 : rattaché à un équipement, daté à l'instant de la déclaration, sans prestataire.
    @Test
    void declarer_rattacheLIncidentALEquipementEtDemarreSansPrestataire() {
        Equipement equipement = incident(1L, IncidentStatut.OUVERT).getEquipement();
        Utilisateur declarant = incident(1L, IncidentStatut.OUVERT).getDeclarant();
        when(equipementRepository.findById(10L)).thenReturn(Optional.of(equipement));
        when(utilisateurRepository.findById(ID_AGENT_AUTEUR)).thenReturn(Optional.of(declarant));
        when(incidentRepository.save(any(Incident.class))).then(AdditionalAnswers.returnsFirstArg());

        IncidentResponse reponse = incidentService.declarer(new IncidentRequest(10L, "  Ascenseur bloqué au deuxième niveau  "), ID_AGENT_AUTEUR);

        assertThat(reponse.idEquipement()).isEqualTo(10L);
        assertThat(reponse.description()).isEqualTo("Ascenseur bloqué au deuxième niveau");
        assertThat(reponse.statut()).isEqualTo(IncidentStatut.OUVERT);
        assertThat(reponse.dateHeureOuverture()).isEqualTo(LocalDateTime.now(horloge));
        assertThat(reponse.dateHeureResolution()).isNull();
        assertThat(reponse.nomPrestataire()).isNull();
        assertThat(equipement.getEtatFonctionnement()).isEqualTo(EquipementEtat.EN_PANNE);
    }

    @Test
    void modifier_responsable_modifieUnIncidentAffecte() {
        Incident incident = incident(1L, IncidentStatut.EN_COURS);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        IncidentResponse reponse = incidentService.modifier(1L, NOUVELLE_DESCRIPTION, RESPONSABLE);

        assertThat(incident.getDescription()).isEqualTo("Ascenseur bloqué au deuxième niveau");
        assertThat(reponse.description()).isEqualTo("Ascenseur bloqué au deuxième niveau");
    }

    @Test
    void modifier_agentAuteur_modifieSonIncidentNonAffecte() {
        Incident incident = incident(1L, IncidentStatut.OUVERT);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        incidentService.modifier(1L, NOUVELLE_DESCRIPTION, AGENT_AUTEUR);

        assertThat(incident.getDescription()).isEqualTo("Ascenseur bloqué au deuxième niveau");
    }

    @Test
    void modifier_agentAuteur_refuseUnIncidentDejaAffecte() {
        verifierRefus(incident(1L, IncidentStatut.EN_COURS), AGENT_AUTEUR,
                "L'incident n°1 est déjà affecté à un prestataire : seul un responsable d'exploitation peut le modifier");
    }

    @Test
    void modifier_autreAgent_refuseUnIncidentQuIlNAPasDeclare() {
        verifierRefus(incident(1L, IncidentStatut.OUVERT), AUTRE_AGENT,
                "Vous ne pouvez modifier que les incidents que vous avez déclarés");
    }

    @Test
    void modifier_incidentResolu_refuseMemePourLeResponsable() {
        verifierRefus(incident(1L, IncidentStatut.RESOLU), RESPONSABLE,
                "L'incident n°1 est résolu : il ne peut plus être modifié");
    }

    @Test
    void incidentsModifiables_agent_neRetientQueSesIncidentsNonAffectes() {
        when(incidentRepository.findByEquipement_IdOrderByDateHeureOuvertureDesc(10L)).thenReturn(List.of(
                incident(1L, IncidentStatut.OUVERT),
                incident(2L, IncidentStatut.EN_COURS),
                incident(3L, IncidentStatut.RESOLU),
                incidentDeclarePar(4L, AUTRE_AGENT.id())));

        assertThat(incidentService.incidentsModifiables(10L, AGENT_AUTEUR)).containsExactly(1L);
    }

    @Test
    void affecter_incidentNonAffecte_leRattacheAuPrestataireEtPasseLEquipementEnMaintenance() {
        Incident incident = incident(1L, IncidentStatut.OUVERT);
        incident.getEquipement().setEtatFonctionnement(EquipementEtat.EN_PANNE);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(prestataireRepository.findById(2L)).thenReturn(Optional.of(prestataire(2L, "Mobilis Maintenance")));

        IncidentResponse reponse = incidentService.affecter(1L, new AffectationRequest(2L), RESPONSABLE);

        assertThat(incident.getStatut()).isEqualTo(IncidentStatut.EN_COURS);
        assertThat(incident.getPrestataire().getId()).isEqualTo(2L);
        assertThat(reponse.nomPrestataire()).isEqualTo("Mobilis Maintenance");
        assertThat(incident.getEquipement().getEtatFonctionnement()).isEqualTo(EquipementEtat.EN_MAINTENANCE);
    }

    @Test
    void affecter_refus_laisseLEquipementEnPanne() {
        Incident incident = incident(1L, IncidentStatut.OUVERT);
        incident.getEquipement().setEtatFonctionnement(EquipementEtat.EN_PANNE);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(prestataireRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incidentService.affecter(1L, new AffectationRequest(99L), RESPONSABLE))
                .isInstanceOf(PrestataireIntrouvableException.class);

        assertThat(incident.getEquipement().getEtatFonctionnement()).isEqualTo(EquipementEtat.EN_PANNE);
    }

    @Test
    void affecter_incidentDejaAffecte_remplaceLePrestataire() {
        Incident incident = incidentAffecteA(1L, prestataire(1L, "AscenTech Services"));
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(prestataireRepository.findById(2L)).thenReturn(Optional.of(prestataire(2L, "Mobilis Maintenance")));

        incidentService.affecter(1L, new AffectationRequest(2L), RESPONSABLE);

        assertThat(incident.getPrestataire().getId()).isEqualTo(2L);
        assertThat(incident.getStatut()).isEqualTo(IncidentStatut.EN_COURS);
    }

    @Test
    void affecter_incidentResolu_refuseSansRienModifier() {
        Incident incident = incidentAffecteA(1L, prestataire(1L, "AscenTech Services"));
        incident.setStatut(IncidentStatut.RESOLU);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> incidentService.affecter(1L, new AffectationRequest(2L), RESPONSABLE))
                .isInstanceOf(AffectationIncidentImpossibleException.class)
                .hasMessage("L'incident n°1 est résolu : il ne peut plus être affecté ni réaffecté");
        assertThat(incident.getPrestataire().getId()).isEqualTo(1L);
        assertThat(incident.getStatut()).isEqualTo(IncidentStatut.RESOLU);
    }

    @Test
    void resoudre_responsableOuTechnicien_cloturePuisRemetLEquipementEnService() {
        for (UtilisateurConnecte autorise : List.of(RESPONSABLE, TECHNICIEN)) {
            Incident incident = incident(1L, IncidentStatut.EN_COURS);
            incident.getEquipement().setEtatFonctionnement(EquipementEtat.EN_PANNE);
            when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

            IncidentDetailResponse reponse = incidentService.resoudre(1L, autorise);

            assertThat(reponse.statut()).isEqualTo(IncidentStatut.RESOLU);
            assertThat(incident.getDateHeureResolution()).isEqualTo(LocalDateTime.now(horloge));
            assertThat(incident.getEquipement().getEtatFonctionnement()).isEqualTo(EquipementEtat.FONCTIONNEL);
        }
    }

    @Test
    void resoudre_autreIncidentAffecte_laisseLEquipementEnMaintenance() {
        Incident incident = incident(1L, IncidentStatut.OUVERT);
        incident.getEquipement().setEtatFonctionnement(EquipementEtat.EN_PANNE);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.existsByEquipement_IdAndStatutAndIdNot(10L, IncidentStatut.EN_COURS, 1L)).thenReturn(true);

        incidentService.resoudre(1L, TECHNICIEN);

        assertThat(incident.getEquipement().getEtatFonctionnement()).isEqualTo(EquipementEtat.EN_MAINTENANCE);
    }

    @Test
    void resoudre_autreIncidentNonAffecte_repasseLEquipementEnPanne() {
        Incident incident = incident(1L, IncidentStatut.EN_COURS);
        incident.getEquipement().setEtatFonctionnement(EquipementEtat.EN_MAINTENANCE);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.existsByEquipement_IdAndStatutNotAndIdNot(10L, IncidentStatut.RESOLU, 1L)).thenReturn(true);

        incidentService.resoudre(1L, TECHNICIEN);

        assertThat(incident.getStatut()).isEqualTo(IncidentStatut.RESOLU);
        assertThat(incident.getEquipement().getEtatFonctionnement()).isEqualTo(EquipementEtat.EN_PANNE);
    }

    @Test
    void resoudre_incidentDejaResolu_refuse() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(1L, IncidentStatut.RESOLU)));

        assertThatThrownBy(() -> incidentService.resoudre(1L, TECHNICIEN))
                .isInstanceOf(IncidentDejaResoluException.class)
                .hasMessage("L'incident n°1 est déjà résolu");
    }

    private void verifierRefus(Incident incident, UtilisateurConnecte utilisateur, String motifAttendu) {
        String descriptionInitiale = incident.getDescription();
        when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> incidentService.modifier(incident.getId(), NOUVELLE_DESCRIPTION, utilisateur))
                .isInstanceOf(ModificationIncidentInterditeException.class)
                .hasMessage(motifAttendu);
        assertThat(incident.getDescription()).isEqualTo(descriptionInitiale);
    }

    private static Incident incident(Long id, IncidentStatut statut) {
        Incident incident = incidentDeclarePar(id, ID_AGENT_AUTEUR);
        incident.setStatut(statut);
        return incident;
    }

    private static Incident incidentDeclarePar(Long id, Long idUtilisateur) {
        Station station = new Station();
        station.setNom("Châtelet");
        Equipement equipement = new Equipement();
        equipement.setId(10L);
        equipement.setReferenceUnique("ASC-CHT-001");
        equipement.setStation(station);
        Utilisateur auteur = new Utilisateur();
        auteur.setId(idUtilisateur);

        Incident incident = new Incident();
        incident.setId(id);
        incident.setDescription("Ascenseur bloqué");
        incident.setStatut(IncidentStatut.OUVERT);
        incident.setDateHeureOuverture(LocalDateTime.now().minusHours(3));
        incident.setEquipement(equipement);
        incident.setDeclarant(auteur);
        return incident;
    }

    private static Incident incidentAffecteA(Long id, Prestataire prestataire) {
        Incident incident = incident(id, IncidentStatut.EN_COURS);
        incident.setPrestataire(prestataire);
        return incident;
    }

    private static Prestataire prestataire(Long id, String nom) {
        Prestataire prestataire = new Prestataire();
        prestataire.setId(id);
        prestataire.setNom(nom);
        prestataire.setContact("contact@exemple.fr");
        return prestataire;
    }
}
