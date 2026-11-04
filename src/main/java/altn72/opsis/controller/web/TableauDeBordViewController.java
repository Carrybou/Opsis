package altn72.opsis.controller.web;

import altn72.opsis.dto.tableaudebord.FiltreActif;
import altn72.opsis.dto.tableaudebord.FiltresTableauDeBord;
import altn72.opsis.dto.tableaudebord.TableauDeBordResponse;
import altn72.opsis.model.equipement.EquipementType;
import altn72.opsis.model.incident.NiveauAttention;
import altn72.opsis.model.utilisateur.UtilisateurType;
import altn72.opsis.security.ProfilRequis;
import altn72.opsis.service.tableaudebord.TableauDeBordInterface;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// F11 : réservé au responsable d'exploitation. Les filtres passent par l'URL : une vue filtrée peut être rechargée ou partagée.
@Controller
public class TableauDeBordViewController {

    private final TableauDeBordInterface tableauDeBordService;

    public TableauDeBordViewController(TableauDeBordInterface tableauDeBordService) {
        this.tableauDeBordService = tableauDeBordService;
    }

    @GetMapping("/tableau-de-bord")
    @ProfilRequis(UtilisateurType.ADMIN)
    public String afficher(@RequestParam(required = false) Long station,
                           @RequestParam(required = false) List<NiveauAttention> niveau,
                           @RequestParam(required = false) EquipementType type,
                           @RequestParam(required = false) String q,
                           @RequestParam(required = false) String tri,
                           Model model) {
        FiltresTableauDeBord filtres = new FiltresTableauDeBord(station, niveau == null ? null : new HashSet<>(niveau), type, q, tri);
        TableauDeBordResponse tableau = tableauDeBordService.consulter(filtres);
        model.addAttribute("tableau", tableau);
        model.addAttribute("filtres", filtres);
        model.addAttribute("filtresActifs", filtresActifs(filtres, tableau));
        model.addAttribute("niveaux", NiveauAttention.values());
        model.addAttribute("types", EquipementType.values());
        return "tableau-de-bord";
    }

    // Une étiquette par filtre appliqué ; son ✕ mène à la même vue sans ce seul filtre.
    private static List<FiltreActif> filtresActifs(FiltresTableauDeBord filtres, TableauDeBordResponse tableau) {
        List<FiltreActif> actifs = new ArrayList<>();
        if (filtres.recherche() != null) {
            actifs.add(new FiltreActif("« " + filtres.recherche() + " »",
                    url(new FiltresTableauDeBord(filtres.idStation(), filtres.niveaux(), filtres.type(), null, filtres.tri()))));
        }
        for (NiveauAttention niveau : filtres.niveaux().stream().sorted().toList()) {
            Set<NiveauAttention> autres = new HashSet<>(filtres.niveaux());
            autres.remove(niveau);
            actifs.add(new FiltreActif(niveau.getLibelle(),
                    url(new FiltresTableauDeBord(filtres.idStation(), autres, filtres.type(), filtres.recherche(), filtres.tri()))));
        }
        if (filtres.idStation() != null) {
            actifs.add(new FiltreActif(tableau.stations().getOrDefault(filtres.idStation(), "Station"),
                    url(new FiltresTableauDeBord(null, filtres.niveaux(), filtres.type(), filtres.recherche(), filtres.tri()))));
        }
        if (filtres.type() != null) {
            actifs.add(new FiltreActif(filtres.type().name(),
                    url(new FiltresTableauDeBord(filtres.idStation(), filtres.niveaux(), null, filtres.recherche(), filtres.tri()))));
        }
        return actifs;
    }

    private static String url(FiltresTableauDeBord filtres) {
        UriComponentsBuilder url = UriComponentsBuilder.fromPath("/tableau-de-bord");
        if (filtres.idStation() != null) {
            url.queryParam("station", filtres.idStation());
        }
        filtres.niveaux().stream().sorted().forEach(n -> url.queryParam("niveau", n.name()));
        if (filtres.type() != null) {
            url.queryParam("type", filtres.type().name());
        }
        if (filtres.recherche() != null) {
            url.queryParam("q", filtres.recherche());
        }
        if (!FiltresTableauDeBord.TRI_ANCIENNETE.equals(filtres.tri())) {
            url.queryParam("tri", filtres.tri());
        }
        return url.encode().build().toUriString();
    }
}
