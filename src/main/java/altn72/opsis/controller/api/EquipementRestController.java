package altn72.opsis.controller.api;

import altn72.opsis.dto.equipement.FicheEquipementResponse;
import altn72.opsis.service.equipement.EquipementInterface;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/equipements")
public class EquipementRestController {

    private final EquipementInterface equipementService;

    public EquipementRestController(EquipementInterface equipementService) {
        this.equipementService = equipementService;
    }

    @GetMapping("/{id}")
    public FicheEquipementResponse getFiche(@PathVariable Long id) {
        return equipementService.consulterFiche(id);
    }
}
