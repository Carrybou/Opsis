package altn72.opsis.dto.incident;

import jakarta.validation.constraints.NotNull;

public class AffectationRequest {

    @NotNull(message = "Veuillez choisir un prestataire")
    private Long idPrestataire;

    public AffectationRequest() {
    }

    public AffectationRequest(Long idPrestataire) {
        this.idPrestataire = idPrestataire;
    }

    public Long getIdPrestataire() {
        return idPrestataire;
    }

    public void setIdPrestataire(Long idPrestataire) {
        this.idPrestataire = idPrestataire;
    }
}
