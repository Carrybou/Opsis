package altn72.opsis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class IncidentRequest {

    @NotNull(message = "Veuillez sélectionner l'équipement concerné")
    @Positive(message = "L'équipement sélectionné est invalide")
    private Long idEquipement;

    @NotBlank(message = "Veuillez décrire l'incident")
    @Size(min = 10, max = 2000, message = "La description doit contenir entre 10 et 2000 caractères")
    private String description;

    public IncidentRequest() {
    }

    public IncidentRequest(Long idEquipement, String description) {
        this.idEquipement = idEquipement;
        this.description = description;
    }

    public Long getIdEquipement() {
        return idEquipement;
    }

    public void setIdEquipement(Long idEquipement) {
        this.idEquipement = idEquipement;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}