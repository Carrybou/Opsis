package altn72.opsis.service.incident;

import altn72.opsis.dto.incident.AffectationRequest;
import altn72.opsis.dto.incident.IncidentDetailResponse;
import altn72.opsis.dto.incident.IncidentModificationRequest;
import altn72.opsis.dto.incident.IncidentRequest;
import altn72.opsis.dto.incident.IncidentResponse;
import altn72.opsis.security.UtilisateurConnecte;

import java.util.Set;

public interface IncidentInterface {
    IncidentResponse declarer(IncidentRequest request, Long idUtilisateur);

    IncidentDetailResponse consulterDetail(Long id);

    IncidentResponse modifier(Long idIncident, IncidentModificationRequest request, UtilisateurConnecte utilisateur);

    boolean estModifiable(Long idIncident, UtilisateurConnecte utilisateur);

    Set<Long> incidentsModifiables(Long idEquipement, UtilisateurConnecte utilisateur);

    IncidentResponse affecter(Long idIncident, AffectationRequest request, UtilisateurConnecte utilisateur);

    boolean estAffectable(Long idIncident, UtilisateurConnecte utilisateur);
}
