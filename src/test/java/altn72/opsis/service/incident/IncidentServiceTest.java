package altn72.opsis.service.incident;

import altn72.opsis.dto.incident.IncidentModificationRequest;
import altn72.opsis.dto.incident.IncidentResponse;
import altn72.opsis.exception.IncidentIntrouvableException;
import altn72.opsis.exception.ModificationIncidentInterditeException;
import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.equipement.EquipementRepository;
import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentRepository;
import altn72.opsis.model.incident.IncidentStatut;
import altn72.opsis.model.station.Station;
import altn72.opsis.model.utilisateur.Utilisateur;
import altn72.opsis.model.utilisateur.UtilisateurRepository;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.UtilisateurConnecte;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @InjectMocks
    private IncidentService incidentService;

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
    void modifier_technicien_refuse() {
        verifierRefus(incident(1L, IncidentStatut.OUVERT), TECHNICIEN,
                "Votre profil ne permet pas de modifier un incident");
    }

    @Test
    void modifier_incidentInexistant_leveIncidentIntrouvableException() {
        when(incidentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incidentService.modifier(99L, NOUVELLE_DESCRIPTION, RESPONSABLE))
                .isInstanceOf(IncidentIntrouvableException.class)
                .hasMessage("L'incident n°99 est introuvable");
    }

    @Test
    void incidentsModifiables_agent_neRetientQueSesIncidentsNonAffectes() {
        when(incidentRepository.findByIdEquipement_IdOrderByDateHeureOuvertureDesc(10L)).thenReturn(List.of(
                incident(1L, IncidentStatut.OUVERT),
                incident(2L, IncidentStatut.EN_COURS),
                incident(3L, IncidentStatut.RESOLU),
                incidentDeclarePar(4L, AUTRE_AGENT.id())));

        assertThat(incidentService.incidentsModifiables(10L, AGENT_AUTEUR)).containsExactly(1L);
    }

    @Test
    void incidentsModifiables_responsable_retientTousLesIncidentsNonResolus() {
        when(incidentRepository.findByIdEquipement_IdOrderByDateHeureOuvertureDesc(10L)).thenReturn(List.of(
                incident(1L, IncidentStatut.OUVERT),
                incident(2L, IncidentStatut.EN_COURS),
                incident(3L, IncidentStatut.RESOLU)));

        assertThat(incidentService.incidentsModifiables(10L, RESPONSABLE)).containsExactlyInAnyOrder(1L, 2L);
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
        equipement.setIdStation(station);
        Utilisateur auteur = new Utilisateur();
        auteur.setId(idUtilisateur);

        Incident incident = new Incident();
        incident.setId(id);
        incident.setDescription("Ascenseur bloqué");
        incident.setStatut(IncidentStatut.OUVERT);
        incident.setDateHeureOuverture(LocalDateTime.now().minusHours(3));
        incident.setIdEquipement(equipement);
        incident.setIdUtilisateur(auteur);
        return incident;
    }
}
