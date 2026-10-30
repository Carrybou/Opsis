package altn72.opsis.service.incident;

import altn72.opsis.dto.incident.IncidentDetailResponse;
import altn72.opsis.dto.incident.IncidentRequest;
import altn72.opsis.dto.incident.IncidentResponse;

public interface IncidentInterface {
    IncidentResponse declarer(IncidentRequest request, Long idUtilisateur);

    IncidentDetailResponse consulterDetail(Long id);
}
