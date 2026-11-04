package altn72.opsis.model.incident;

import java.time.LocalDateTime;

// Projection de IncidentRepository.compterDeclarationsParUtilisateur : une ligne par utilisateur ayant déclaré au moins un incident.
public interface DeclarationsUtilisateur {

    Long getIdUtilisateur();

    Number getDeclares();

    Number getActifs();

    LocalDateTime getDerniereDeclaration();
}
