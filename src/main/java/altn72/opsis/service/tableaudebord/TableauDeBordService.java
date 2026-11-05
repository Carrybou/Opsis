package altn72.opsis.service.tableaudebord;

import altn72.opsis.dto.tableaudebord.FiltresTableauDeBord;
import altn72.opsis.dto.tableaudebord.IncidentActifResponse;
import altn72.opsis.dto.tableaudebord.TableauDeBordResponse;
import altn72.opsis.dto.tableaudebord.TableauDeBordResponse.*;
import altn72.opsis.model.equipement.Equipement;
import altn72.opsis.model.equipement.EquipementEtat;
import altn72.opsis.model.equipement.EquipementRepository;
import altn72.opsis.model.equipement.EquipementType;
import altn72.opsis.model.incident.Incident;
import altn72.opsis.model.incident.IncidentRepository;
import altn72.opsis.model.incident.NiveauAttention;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class TableauDeBordService implements TableauDeBordInterface {

    private static final int JOURS_COURBE = 30;
    private static final int JOURS_TENDANCE = 7;
    private static final int HEURES_PAR_JOUR = 24;
    private static final int HEURES_PAR_SEMAINE = JOURS_TENDANCE * HEURES_PAR_JOUR;

    // Tranches de l'histogramme d'âge : bornes en heures et couleur RG3 d'un incident sans prestataire de cette tranche.
    private static final List<Tranche> TRANCHES = List.of(
            new Tranche("< " + HEURES_PAR_JOUR + " h", 0, HEURES_PAR_JOUR),
            new Tranche(HEURES_PAR_JOUR + "-" + NiveauAttention.HEURES_SEUIL_ORANGE + " h", HEURES_PAR_JOUR, NiveauAttention.HEURES_SEUIL_ORANGE),
            new Tranche(NiveauAttention.HEURES_SEUIL_ORANGE + "-" + NiveauAttention.HEURES_SEUIL_ROUGE + " h", NiveauAttention.HEURES_SEUIL_ORANGE, NiveauAttention.HEURES_SEUIL_ROUGE),
            new Tranche(NiveauAttention.HEURES_SEUIL_ROUGE / HEURES_PAR_JOUR + "-" + JOURS_TENDANCE + " j", NiveauAttention.HEURES_SEUIL_ROUGE, HEURES_PAR_SEMAINE),
            new Tranche("> " + JOURS_TENDANCE + " j", HEURES_PAR_SEMAINE, Long.MAX_VALUE));

    private record Tranche(String libelle, long debutHeures, long finHeures) {
        boolean contient(Duration ouvert) {
            return ouvert.toHours() >= debutHeures && ouvert.toHours() < finHeures;
        }
    }

    private final IncidentRepository incidentRepository;
    private final EquipementRepository equipementRepository;
    private final Clock horloge;

    public TableauDeBordService(IncidentRepository incidentRepository, EquipementRepository equipementRepository, Clock horloge) {
        this.incidentRepository = incidentRepository;
        this.equipementRepository = equipementRepository;
        this.horloge = horloge;
    }

    // F11 / RG6 : la liste et les indicateurs ne portent que sur les incidents non résolus ; les résolus servent
    // seulement à l'historique (courbe sur 30 jours, résolus en 7 jours). Le niveau est recalculé à chaque affichage (RG3).
    @Override
    @Transactional(readOnly = true)
    public TableauDeBordResponse consulter(FiltresTableauDeBord filtres) {
        LocalDateTime maintenant = LocalDateTime.now(horloge);
        List<Incident> tous = incidentRepository.findAll();
        List<Equipement> equipements = equipementRepository.findAll();

        List<IncidentActifResponse> actifs = tous.stream()
                .filter(incident -> !incident.estResolu())
                .map(incident -> IncidentActifResponse.from(incident, maintenant))
                .toList();

        // Périmètre hors niveau : sert aux compteurs des puces de niveau, qui doivent rester visibles quand on en coche une.
        List<IncidentActifResponse> perimetre = actifs.stream().filter(perimetreActif(filtres)).toList();
        List<IncidentActifResponse> selection = perimetre.stream()
                .filter(i -> filtres.niveaux().isEmpty() || filtres.niveaux().contains(i.niveau()))
                .toList();
        List<Incident> historique = tous.stream().filter(perimetreHistorique(filtres)).toList();
        List<Equipement> parc = equipements.stream().filter(perimetreParc(filtres)).toList();

        return new TableauDeBordResponse(
                maintenant,
                indicateurs(selection, historique, parc, maintenant, !filtres.niveaux().isEmpty()),
                compterParNiveau(perimetre),
                tranchesAge(selection, maintenant),
                chargeParStation(selection),
                courbeActifs(historique, maintenant),
                grouper(selection, filtres.tri()),
                selection.size(),
                nomsDesStations(equipements)
        );
    }

    private static Predicate<Equipement> perimetreParc(FiltresTableauDeBord filtres) {
        return e -> dansLeFiltre(filtres, e.getStation().getId(), e.getType());
    }

    private static boolean dansLeFiltre(FiltresTableauDeBord filtres, Long idStation, EquipementType type) {
        return (filtres.idStation() == null || filtres.idStation().equals(idStation))
                && (filtres.type() == null || filtres.type() == type);
    }

    private static Map<Long, String> nomsDesStations(List<Equipement> equipements) {
        return equipements.stream().collect(Collectors.toMap(e -> e.getStation().getId(), e -> e.getStation().getNom(),
                (a, b) -> a, TreeMap::new));
    }

    private static Predicate<IncidentActifResponse> perimetreActif(FiltresTableauDeBord filtres) {
        return i -> dansLeFiltre(filtres, i.idStation(), i.typeEquipement())
                && correspond(filtres.recherche(), i.referenceEquipement(), i.description(), i.nomStation());
    }

    private static Predicate<Incident> perimetreHistorique(FiltresTableauDeBord filtres) {
        return incident -> {
            Equipement equipement = incident.getEquipement();
            return dansLeFiltre(filtres, equipement.getStation().getId(), equipement.getType())
                    && correspond(filtres.recherche(), equipement.getReferenceUnique(), incident.getDescription(),
                    equipement.getStation().getNom());
        };
    }

    // Recherche insensible à la casse et aux accents (« ecran » trouve « Écran »).
    private static boolean correspond(String recherche, String... champs) {
        if (recherche == null) {
            return true;
        }
        String cherche = normaliser(recherche);
        for (String champ : champs) {
            if (champ != null && normaliser(champ).contains(cherche)) {
                return true;
            }
        }
        return false;
    }

    private static String normaliser(String texte) {
        return Normalizer.normalize(texte, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    private static Indicateurs indicateurs(List<IncidentActifResponse> selection, List<Incident> historique,
                                           List<Equipement> parc, LocalDateTime maintenant, boolean filtreParNiveau) {
        LocalDateTime debutSemaine = maintenant.minusDays(JOURS_TENDANCE);
        LocalDateTime debutSemainePrecedente = debutSemaine.minusDays(JOURS_TENDANCE);
        long actifsDebutSemaine = historique.stream().filter(i -> etaitActif(i, debutSemaine)).count();
        return new Indicateurs(
                selection.size(),
                filtreParNiveau ? null : historique.stream().filter(i -> etaitActif(i, maintenant)).count() - actifsDebutSemaine,
                selection.stream().filter(IncidentActifResponse::estSansPrestataire).count(),
                selection.stream().filter(i -> i.niveau() == NiveauAttention.ROUGE).count(),
                parc.stream().filter(e -> e.getEtatFonctionnement() != EquipementEtat.FONCTIONNEL).count(),
                parc.stream().filter(e -> e.getEtatFonctionnement() == EquipementEtat.EN_MAINTENANCE).count(),
                parc.size(),
                historique.stream().filter(i -> resoluEntre(i, debutSemaine, maintenant)).count(),
                historique.stream().filter(i -> resoluEntre(i, debutSemainePrecedente, debutSemaine)).count()
        );
    }

    private static boolean etaitActif(Incident incident, LocalDateTime instant) {
        return !incident.getDateHeureOuverture().isAfter(instant)
                && (incident.getDateHeureResolution() == null || incident.getDateHeureResolution().isAfter(instant));
    }

    private static boolean resoluEntre(Incident incident, LocalDateTime debut, LocalDateTime fin) {
        LocalDateTime resolution = incident.getDateHeureResolution();
        return incident.estResolu() && resolution != null && resolution.isAfter(debut) && !resolution.isAfter(fin);
    }

    private static Map<NiveauAttention, Long> compterParNiveau(List<IncidentActifResponse> incidents) {
        Map<NiveauAttention, Long> compte = new EnumMap<>(NiveauAttention.class);
        for (NiveauAttention niveau : NiveauAttention.values()) {
            compte.put(niveau, incidents.stream().filter(i -> i.niveau() == niveau).count());
        }
        return compte;
    }

    private static List<TrancheAge> tranchesAge(List<IncidentActifResponse> selection, LocalDateTime maintenant) {
        List<List<IncidentActifResponse>> parTranche = TRANCHES.stream()
                .map(tranche -> selection.stream()
                        .filter(i -> tranche.contient(Duration.between(i.dateHeureOuverture(), maintenant)))
                        .toList())
                .toList();
        long totalMaximal = parTranche.stream().mapToLong(List::size).max().orElse(0);

        List<TrancheAge> tranches = new ArrayList<>();
        for (int indice = 0; indice < TRANCHES.size(); indice++) {
            Tranche tranche = TRANCHES.get(indice);
            List<IncidentActifResponse> dedans = parTranche.get(indice);
            long sansPrestataire = dedans.stream().filter(IncidentActifResponse::estSansPrestataire).count();
            tranches.add(new TrancheAge(tranche.libelle(),
                    NiveauAttention.selonAnciennete(Duration.ofHours(tranche.debutHeures())),
                    sansPrestataire, dedans.size() - sansPrestataire, totalMaximal));
        }
        return tranches;
    }

    // Stations classées de la plus chargée à la moins chargée ; chaque barre est découpée par niveau, du plus urgent au moins urgent.
    private static List<ChargeStation> chargeParStation(List<IncidentActifResponse> selection) {
        Map<Long, List<IncidentActifResponse>> parStation = selection.stream()
                .collect(Collectors.groupingBy(IncidentActifResponse::idStation));
        long totalMaximal = parStation.values().stream().mapToLong(List::size).max().orElse(0);

        return parStation.values().stream()
                .sorted(Comparator.<List<IncidentActifResponse>>comparingInt(List::size).reversed()
                        .thenComparing(liste -> liste.getFirst().nomStation()))
                .map(liste -> new ChargeStation(liste.getFirst().idStation(), liste.getFirst().nomStation(), liste.size(),
                        segments(liste), totalMaximal))
                .toList();
    }

    private static List<Segment> segments(List<IncidentActifResponse> incidentsStation) {
        List<Segment> segments = new ArrayList<>();
        for (NiveauAttention niveau : NiveauAttention.values()) {
            long nombre = incidentsStation.stream().filter(i -> i.niveau() == niveau).count();
            if (nombre > 0) {
                segments.add(new Segment(niveau, nombre, incidentsStation.size()));
            }
        }
        return segments;
    }

    // Nombre d'incidents actifs à la fin de chacun des 30 derniers jours (aujourd'hui : à l'instant présent).
    private static Courbe courbeActifs(List<Incident> historique, LocalDateTime maintenant) {
        LocalDate aujourdhui = maintenant.toLocalDate();
        SortedMap<LocalDate, Long> actifsParJour = new TreeMap<>();
        for (int joursEnArriere = JOURS_COURBE - 1; joursEnArriere >= 0; joursEnArriere--) {
            LocalDate jour = aujourdhui.minusDays(joursEnArriere);
            LocalDateTime instant = joursEnArriere == 0 ? maintenant : jour.atTime(LocalTime.MAX);
            actifsParJour.put(jour, historique.stream().filter(i -> etaitActif(i, instant)).count());
        }
        return Courbe.de(actifsParJour);
    }

    private static List<GroupeIncidents> grouper(List<IncidentActifResponse> selection, String tri) {
        Comparator<IncidentActifResponse> plusAncien = Comparator.comparing(IncidentActifResponse::dateHeureOuverture);
        Comparator<IncidentActifResponse> ordre = switch (tri) {
            case FiltresTableauDeBord.TRI_RECENT -> plusAncien.reversed();
            case FiltresTableauDeBord.TRI_STATION -> Comparator.comparing(IncidentActifResponse::nomStation).thenComparing(plusAncien);
            default -> plusAncien;
        };
        List<GroupeIncidents> groupes = new ArrayList<>();
        for (NiveauAttention niveau : NiveauAttention.values()) {
            List<IncidentActifResponse> incidents = selection.stream().filter(i -> i.niveau() == niveau).sorted(ordre).toList();
            if (!incidents.isEmpty()) {
                groupes.add(new GroupeIncidents(niveau, incidents));
            }
        }
        return groupes;
    }
}
