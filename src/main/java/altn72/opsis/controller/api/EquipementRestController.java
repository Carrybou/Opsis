package altn72.opsis.controller.api;

import altn72.opsis.dto.equipement.FicheEquipementResponse;
import altn72.opsis.service.equipement.EquipementInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Équipements")
@RestController
@RequestMapping("/api/equipements")
public class EquipementRestController {

    private final EquipementInterface equipementService;

    public EquipementRestController(EquipementInterface equipementService) {
        this.equipementService = equipementService;
    }

    @Operation(summary = "Consulter la fiche d'un équipement", description = "Avec ses incidents en cours et passés, par date d'ouverture.")
    @ApiResponse(responseCode = "200", description = "Fiche de l'équipement")
    @ApiResponse(responseCode = "404", description = "Équipement introuvable")
    @GetMapping("/{id}")
    public FicheEquipementResponse consulterFiche(@PathVariable Long id) {
        return equipementService.consulterFiche(id);
    }
}
