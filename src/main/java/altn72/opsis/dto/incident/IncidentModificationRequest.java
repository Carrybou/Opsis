package altn72.opsis.dto.incident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class IncidentModificationRequest {

    @NotBlank(message = "Veuillez décrire l'incident")
    @Size(min = 10, max = 2000, message = "La description doit contenir entre 10 et 2000 caractères")
    private String description;

    public IncidentModificationRequest() {
    }

    public IncidentModificationRequest(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
