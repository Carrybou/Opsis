package altn72.opsis.service.incident;

import altn72.opsis.dto.incident.IncidentModificationRequest;
import altn72.opsis.dto.incident.IncidentRequest;
import altn72.opsis.dto.incident.IncidentResponse;
import altn72.opsis.security.UtilisateurConnecte;

import java.util.Set;

public interface IncidentInterface {
    IncidentResponse declarer(IncidentRequest request, Long idUtilisateur);

    IncidentResponse trouverPourModification(Long idIncident, UtilisateurConnecte utilisateur);

    IncidentResponse modifier(Long idIncident, IncidentModificationRequest request, UtilisateurConnecte utilisateur);

    Set<Long> incidentsModifiables(Long idEquipement, UtilisateurConnecte utilisateur);
}
