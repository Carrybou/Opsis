package altn72.opsis.service.incident;

import altn72.opsis.dto.IncidentRequest;
import altn72.opsis.dto.IncidentResponse;

public interface IncidentInterface {
    IncidentResponse declarer(IncidentRequest request, Long idUtilisateur);
}
