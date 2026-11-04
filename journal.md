# Journal — Opsis (ALTN72, 2026-27)

**Groupe :** Thomas GUIBERT, Roméo FROGER

## LANCER L'APPLICATION EN 3 COMMANDES
Prérequis : Java 25, Docker Desktop démarré, et un fichier `.env` à la racine (voir le tableau du README ci-dessous).

```bash
docker compose up -d                      # 1. base MySQL (port 3307) + Adminer (http://localhost:8081)
set -a; source .env; set +a               # 2. charge les variables (JWT_SECRET, identifiants MySQL)
DB_PORT=3307 sh mvnw spring-boot:run      # 3. lance l'application sur le MySQL Docker
```

Ouvrir ensuite http://localhost:8080 et se connecter avec un des comptes de test (voir README ci-dessous).

En cas de problème :
- **`Port 8080 was already in use`** : une ancienne instance tourne encore. `lsof -ti:8080` donne son PID, puis `kill <PID>`.
- **`Unable to determine Dialect without JDBC metadata`** : l'application ne joint pas MySQL. Vérifier que le conteneur tourne (`docker ps`) et que `DB_PORT=3307` est bien passé : sans cette variable, l'application cherche un MySQL local sur le port 3306.
- **Erreur sur `jwt.secret`** : le `.env` n'a pas été chargé (étape 2), ou VS Code / IntelliJ lance l'application sans lui. Il faut alors ajouter le `.env` dans la configuration de lancement de l'IDE.
- **Repartir d'une base propre** : `docker compose down -v && docker compose up -d`.

## README
- Prérequis : Java 25, Maven 3.9+, Docker (Docker Desktop ou Docker Engine + Compose).
- Configuration : créer un fichier `.env` à la racine (non versionné) contenant :

  | Variable              | Rôle                                                        |
  |:----------------------|:------------------------------------------------------------|
  | `MYSQL_ROOT_PASSWORD` | Mot de passe root du conteneur MySQL                        |
  | `MYSQL_DATABASE`      | `Base_ALTN72`                                               |
  | `MYSQL_USER`          | Utilisateur applicatif de la base                           |
  | `MYSQL_PASSWORD`      | Mot de passe de cet utilisateur                             |
  | `JWT_SECRET`          | Clé de signature des JWT de l'API (32 caractères minimum)   |

- Initialiser la base : `docker compose up -d`
  - MySQL 8 est exposé sur le port **3307**, Adminer sur http://localhost:8081. L'application se connecte par défaut au port 3306 (MySQL local) ; pour le MySQL Docker, lancer avec `DB_PORT=3307`.
  - `bdd.sql` (schéma + données de démonstration) n'est joué qu'à la première création du volume. Pour repartir d'une base propre : `docker compose down -v && docker compose up -d`.
- Lancer : voir « Lancer l'application en 3 commandes » en haut du journal (`.env` chargé, `DB_PORT=3307`). L'application est alors disponible sur http://localhost:8080 (redirection vers `/login` si non connecté).
- Application déployée : https://...
- Comptes de test (créés par `bdd.sql`) :

  | Identifiant | Profil | Mot de passe |
  |:--|:--|:--|
  | `admin` | Responsable d'exploitation | ... |
  | `agent.bernard`, `agent.petit`, `agent.moreau` | Agent | ... |
  | `tech.martin`, `tech.dubois` | Technicien | ... |
- Limites connues :
  - Les dates des données de démonstration sont relatives au moment où la base est créée (`NOW() - INTERVAL …` dans `bdd.sql`). Elles « vieillissent » ensuite : recréer la base (`docker compose down -v && docker compose up -d`) pour retrouver un tableau de bord avec les trois couleurs.
  - Les identifiants de la base sont encore écrits dans `application.properties` au lieu d'être lus depuis les variables d'environnement (§7.7).

## ARCHITECTURE
- Dépôt : https://github.com/Carrybou/Opsis
- Découpage en packages (`altn72.opsis`) :

  | Package            | Responsabilité                                                                  |
  |:-------------------|:--------------------------------------------------------------------------------|
  | `controller.web`   | Contrôleurs Thymeleaf (session HTTP), gestion des erreurs côté pages            |
  | `controller.api`   | Contrôleurs REST sous `/api/**` (authentification JWT)                          |
  | `service`          | Règles métier, exposées par des interfaces (`EquipementInterface`…)            |
  | `model`            | Entités JPA et repositories Spring Data, regroupés par domaine                  |
  | `dto`              | Objets échangés avec les vues et l'API (records)                                |
  | `exception`        | Exceptions métier et advices (API en JSON, pages d'erreur)                      |
  | `security`         | Intercepteurs session / JWT, contrôle des profils (`@ProfilRequis`)             |
  | `config`           | Enregistrement des intercepteurs, beans de sécurité                             |

### Schéma  
![architecture](docs/Architecture.png)

### MCD
![mcd](docs/MCD.png)
## TESTS
- Stratégie : peu de tests, mais ciblés sur ce qui casserait l'application si on se trompait. On garde :
  - chaque règle métier (RG) une fois, dans le test du service ;
  - le refus par profil de chaque route protégée (RG11) ;
  - le cas nominal de chaque action ;
  - un exemple de chaque code d'erreur de l'API (F12) ;
  - un seul test de formulaire invalide, le mécanisme étant le même partout.

  Les vérifications purement visuelles (bouton affiché, étoiles, menu) sont faites à la main dans le navigateur plutôt que figées dans des tests à maintenir.
- Outils :
  - Services : JUnit 5 + Mockito, repositories simulés, aucune base nécessaire.
  - Contrôleurs : `@WebMvcTest` + MockMvc. Seule la couche web est chargée (contrôleurs, advices, intercepteurs de session et JWT), les services sont des `@MockitoBean`. Les templates Thymeleaf sont réellement rendus.
  - Un test `@SpringBootTest` vérifie que le contexte complet démarre (base réelle).
- Commande : `DB_PORT=3307 mvn test` (base démarrée et `.env` chargé, pour `OpsisApplicationTests`).
- Couvert (66 tests, tous verts) :

  | Domaine | Tests | Ce qui est vérifié |
  |:--|:--|:--|
  | F1 connexion | `AuthServiceTest`, `LoginViewControllerTest` | identifiants valides ; même message pour un identifiant inconnu et un mauvais mot de passe ; ouverture de session, erreur réaffichée, déconnexion |
  | F2–F3 stations, équipements | `StationViewControllerTest`, `EquipementServiceTest`, `EquipementViewControllerTest` | redirection vers `/login` sans session ; regroupement par station et synthèse ; incidents en cours / passés dans l'ordre (RG7) ; boutons Déclarer / Modifier selon le profil |
  | F4 déclaration | `IncidentViewControllerTest` | responsable : incident enregistré ; technicien : 403 |
  | F6 modification | `IncidentServiceTest`, `IncidentViewControllerTest` | responsable : tout incident non résolu ; agent : seulement ses incidents non affectés ; incident résolu figé (RG5) ; 403 technicien ; description invalide réaffichée |
  | F7 affectation | `IncidentServiceTest`, `IncidentViewControllerTest` | affectation → `EN_COURS` et équipement en maintenance ; réaffectation ; incident résolu refusé (RG5) ; refus sans rien modifier (RG10) ; 403 agent / technicien |
  | F8 résolution | `IncidentServiceTest`, `IncidentViewControllerTest` | clôture et état de l'équipement selon les incidents restants (fonctionnel, en maintenance, en panne) ; déjà résolu ; 403 agent |
  | F10 évaluation | `EvaluationServiceTest`, `PrestataireServiceTest`, `IncidentViewControllerTest`, `PrestataireViewControllerTest` | RG8 : évaluation enregistrée, refus si non résolu, sans prestataire ou déjà évalué ; note hors de 1 à 5 refusée ; moyenne de la requête native rattachée au bon prestataire ; fiche prestataire ; 403 agent / technicien |
  | F11 tableau de bord | `NiveauAttentionTest`, `TableauDeBordServiceTest`, `TableauDeBordViewControllerTest` | RG3 aux bornes exactes (47 h vert, 48 h pile orange, 72 h pile rouge) ; RG4 : affecté jamais vert / orange / rouge ; groupes par urgence, échéance avant le seuil suivant (« orange dans 4 h ») ; filtres appliqués aux indicateurs et à la liste, compteurs des puces sur le périmètre ; RG6 (résolu exclu) ; 403 agent ; filtres de l'URL (niveaux multiples, recherche, tri) transmis au service |
  | Lot alpha, cartographie | `CarteServiceTest`, `CarteViewControllerTest` | situation d'une station selon son équipement le plus grave, tri par gravité ; station sans équipement ni coordonnées conservée ; incidents actifs rattachés à leur équipement ; redirection vers `/login` sans session ; coordonnées et liens vers équipements et incidents dans la page |
  | F12 API | `IncidentRestControllerTest` | 401 sans token, 403 profil, 404 incident inexistant, 400 donnée invalide, 409 état incompatible, 200 / 201 sur chaque opération |

- Non couvert : repositories (`@DataJpaTest`), prévus dans le lot alpha « Qualité renforcée » ; documentation Swagger de l'API (F12) non encore réalisée.

## CHOIX TECHNIQUES
### Droits par profil

| N°    | Fonctionnalité                        | Profil 1 : Responsable d'exploitation | Profil 2 : Agent                                        |
|:------|:--------------------------------------|:--------------------------------------|:--------------------------------------------------------|
| F2–F3 | Consulter équipements et fiches       | Lecture seule                         | Lecture seule                                           |
| F4    | Déclarer un incident                  | Accès complet                         | Accès complet                                           |
| F5–F6 | Consulter / modifier un incident      | Accès complet                         | Consultation ; modification de ses incidents non affectés |
| F7    | Affecter ou réaffecter un prestataire | Accès complet                         | Lecture seule (prestataire affiché)                     |
| F8    | Déclarer un incident résolu           | Accès complet                         | Interdit                                                |
| F9    | Consulter l'historique                | Lecture seule                         | Lecture seule                                           |
| F10   | Évaluer un prestataire                | Accès complet                         | Lecture seule (notes et fiches prestataire)             |
| F11   | Tableau de bord                       | Accès complet                         | Interdit                                                |

- Technicien (troisième profil, absent du tableau du sujet) : consultation de tout ; il ne peut ni déclarer ni modifier un incident (F4, F6), ni affecter (F7), ni ouvrir le tableau de bord (F11), mais il peut le déclarer résolu (F8). Il ne peut pas évaluer (F10) : c'est lui qui intervient, il ne doit pas noter son propre travail.
- F6 pour l'agent : il ne modifie que les incidents qu'il a déclarés, tant qu'aucun prestataire n'est affecté. Un incident résolu n'est plus modifiable par personne (RG5).
- F7 : un incident résolu ne peut plus être affecté ni réaffecté (RG5). Une fois l'incident affecté, l'agent qui l'a déclaré ne peut plus modifier sa description (F6).
- F10 : seulement une fois l'incident résolu et s'il a été affecté à un prestataire (RG8).
- Double contrôle (RG11) : `@ProfilRequis` filtre la route selon le profil (intercepteur session ou JWT), puis le service vérifie les règles qui dépendent de l'incident (auteur, affectation, statut). Les vues n'affichent un bouton que si le service le permet (`estModifiable`, `estAffectable`, `estEvaluable`) : elles s'appuient sur la même règle, mais la sécurité reste côté serveur.

### Règles et interface
- État de l'équipement selon ses incidents (choix du groupe, le sujet n'impose pas de règle) : `EN_PANNE` à la déclaration, `EN_MAINTENANCE` à l'affectation d'un prestataire. À la résolution, il dépend des incidents restants : `EN_MAINTENANCE` s'il en reste un affecté, sinon `EN_PANNE` s'il en reste un non affecté, sinon `FONCTIONNEL`.
- Actions sur la page détail d'un incident : modification (F6), affectation (F7) et évaluation (F10) se font directement dans la carte concernée, sans changer de page. Un bouton ouvre le formulaire sur place ; l'envoi revient sur la même page avec un message de confirmation ; une erreur de validation réaffiche la page avec le formulaire ouvert. Sans JavaScript, chaque bouton reste un lien (`?modifier=true`, `?affecter=true`, `?evaluer=true`) qui ouvre le formulaire côté serveur.
  - F6 : `PUT /incidents/{id}` (champ caché `_method`), description préremplie avec compteur de caractères.
  - F7 : `PUT /incidents/{id}/prestataire`. Le bouton s'appelle « Affecter » ou « Réaffecter » ; lors d'une réaffectation, le prestataire actuel est marqué « (actuel) » et ne peut pas être choisi.
  - F10 : `POST /incidents/{id}/evaluation`. 5 étoiles qui sont de vrais boutons radio masqués (utilisables au clavier et sans JavaScript), commentaire facultatif. Une évaluation n'est plus modifiable ensuite.
- Où consulter la note (F10) : colonne « Évaluation » de l'historique (F9) ; fiche prestataire (`/prestataires/{id}` : note moyenne, nombre d'évaluations, interventions en cours et terminées avec leur note) ; note moyenne dans la carte Prestataire de la page détail et dans la liste d'affectation (F7), pour que le responsable choisisse en connaissant la qualité des prestataires. Les noms de prestataires mènent à leur fiche.
- Tableau de bord (F11, `/tableau-de-bord`, responsable uniquement, page d'arrivée après connexion pour ce profil). Objectif : dire en quelques secondes où est le problème et quoi faire ensuite.
  - **Indicateurs** : incidents actifs (avec leur variation sur 7 jours), incidents sans prestataire (dont rouges), équipements hors service (nombre, part du parc, jauge panne / maintenance), incidents résolus en 7 jours comparés à la semaine précédente.
  - **Graphiques**, rendus côté serveur en HTML / SVG, sans bibliothèque JavaScript ni CDN :
    - âge des incidents actifs : colonnes par tranche (< 24 h, 24-48 h, 48-72 h, 3-7 j, > 7 j), la couleur RG3 de la tranche pour les incidents sans prestataire, le bleu pour ceux pris en charge, empilé au-dessus ;
    - charge par station : barres découpées par niveau, triées de la plus chargée à la moins chargée ; un clic filtre sur la station ;
    - incidents actifs sur 30 jours : courbe du stock en fin de journée, pour voir s'il monte (on ouvre plus qu'on ne résout) ou baisse.

    Chaque graphique a une infobulle au survol et au clavier, une légende, et un tableau « Voir les données » (la couleur n'est jamais la seule information). Les quatre couleurs de niveau ont été vérifiées avec un outil de contrôle des palettes : distinctes en vision normale et pour les principaux daltonismes.
  - **Liste groupée par urgence** : Critique, À surveiller, Récents, puis Pris en charge (replié, car il n'y a rien à faire). Pour chaque incident sans prestataire, une barre montre où il en est avant le seuil suivant : « orange dans 2 h 57 min », « rouge dans 10 h », ou « en retard de 6 j » pour un rouge. On passe d'une couleur subie à une échéance qu'on peut anticiper.
  - **Filtres** sur une seule ligne, au-dessus de tout ce qu'ils filtrent (indicateurs, graphiques et liste) : recherche (référence, description, station, sans tenir compte des accents), niveaux en puces cumulables libellées par durée (« < 48 h », « 48 – 72 h », « 72 h + », « Pris en charge ») qui servent aussi de légende (couleur, durée, nombre), un « ? » qui explique les seuils, station et type d'équipement. Les filtres appliqués s'affichent en étiquettes retirables une à une, avec « Tout effacer ». Ils passent dans l'URL (`GET`), donc une vue filtrée se recharge et se partage.
  - **Niveau d'attention** (RG3 / RG4) : `NiveauAttention.calculer(incident, maintenant)` dans le modèle, avec des `Duration` exactes (pas des heures arrondies). Rien n'est stocké en base. L'heure vient d'un bean `Clock` injecté, ce qui permet de tester les bornes avec une horloge fixe. Seuls les incidents non résolus apparaissent dans la liste et les niveaux (RG6).
  - **Fraîcheur** : « Mis à jour il y a X min », et la page se recharge toute seule toutes les 5 minutes (sauf pendant une saisie), puisque les niveaux dépendent de l'heure.
- Navigation : la barre du haut contient les pages principales (Tableau de bord pour le responsable, Stations). Les pages secondaires (Historique, Prestataires) et la déconnexion sont regroupées dans un menu ouvert en cliquant sur le profil (initiale, prénom et libellé du profil en clair). Le menu est un `<details>` : il fonctionne sans JavaScript, et un petit script le referme au clic en dehors ou sur Échap.
- API REST (F12) : `GET /api/incidents/{id}`, `GET /api/incidents/historique`, `POST /api/incidents`, `PATCH /api/incidents/{id}` (description), `PUT /api/incidents/{id}/prestataire` (`{"idPrestataire": 2}`), `POST /api/incidents/{id}/evaluation` (`{"note": 4, "commentaire": "..."}`), `POST /api/incidents/{id}/resolution` (F8). Authentification par JWT, mêmes droits que les pages.

### Exceptions : @ControllerAdvice
- Les exceptions métier sont personnalisées, avec un message en français, et héritent d'une classe mère par type d'erreur (`RessourceIntrouvableException` → `EquipementIntrouvableException`, `IncidentIntrouvableException`, `PrestataireIntrouvableException`…).
- Deux advices distincts, ciblés par `basePackages` : `ApiExceptionHandler` (`controller.api`) traduit l'erreur en JSON `ApiError` avec le bon code HTTP ; `VueExceptionHandler` (`controller.web`) affiche la page `erreur.html` avec un message lisible, sans trace technique.
- Codes utilisés :

  | Code | Cas | Exception |
  |:--|:--|:--|
  | 400 | donnée invalide (description, note hors de 1 à 5, prestataire manquant) | validation `@Valid` |
  | 401 | API sans token ou token invalide | intercepteur JWT |
  | 403 | profil non autorisé, ou règle F6 non respectée (avec le motif) | `@ProfilRequis`, `AccesRefuseException`, `ModificationIncidentInterditeException` |
  | 404 | incident, équipement ou prestataire inexistant | `RessourceIntrouvableException` et ses filles |
  | 409 | l'état de l'incident empêche l'opération : déjà résolu, affectation impossible (résolu, même prestataire), évaluation impossible (non résolu, sans prestataire, déjà évalué) | `IncidentDejaResoluException`, `AffectationIncidentImpossibleException`, `EvaluationImpossibleException` |

- Pourquoi pas `ResponseStatusException` : elle obligerait chaque contrôleur ou service à choisir le code HTTP lui-même et lierait la couche métier au web. Avec un advice, le service lève une exception métier, et la traduction HTTP est faite à un seul endroit, différemment pour l'API et pour les pages.
- `ResponseEntity` est utilisé dans l'advice de l'API pour fixer à la fois le code HTTP et le corps `ApiError`.
- L'unicité d'une évaluation par incident (RG8) est vérifiée par le service avant l'insertion, avec un message clair ; la contrainte `UNIQUE` de la base reste une sécurité.

### SOLID
- **S — Responsabilité unique** : contrôleurs web et REST séparés ; les contrôleurs délèguent au service, qui porte la règle métier. Un service par domaine (`IncidentService`, `EvaluationService`, `PrestataireService`, `TableauDeBordService`, `EquipementService`…) : l'évaluation a son propre service plutôt que d'alourdir `IncidentService`.
- **O — Ouvert/fermé** : les advices traitent `RessourceIntrouvableException` ; une nouvelle ressource introuvable (prestataire…) produit un 404 sans modifier les handlers.
- **D — Inversion des dépendances** : les contrôleurs dépendent des interfaces de service (`IncidentInterface`, `EvaluationInterface`, `PrestataireInterface`, `TableauDeBordInterface`, `EquipementInterface`, `AuthInterface`…), injectées par constructeur. Le même service sert la vue et l'API, et les services se testent avec des repositories simulés.
- L, I : ...

### Requête SQL native
`EvaluationRepository.calculerNotesParPrestataire` : moyenne (`AVG`) et nombre (`COUNT`) des évaluations par prestataire, avec une jointure `evaluation` → `incident` et un `GROUP BY id_prestataire`. Le calcul est fait par la base en une requête, au lieu de charger toutes les évaluations puis leurs incidents en Java. Le résultat est lu par une projection (`NotePrestataire`) dont les valeurs sont typées `Number`, car MySQL renvoie `AVG` en `DECIMAL` et `COUNT` en `BIGINT`.

### @Transactional (RG10)
- `IncidentService.declarer` : la création de l'incident et le passage de l'équipement à « en panne » sont enregistrés ensemble ou pas du tout.
- `IncidentService.modifier` : l'incident chargé reste suivi par JPA, donc la nouvelle description est enregistrée à la fin de la méthode sans `save` explicite.
- `IncidentService.affecter` : le prestataire, le statut `EN_COURS` et l'état de l'équipement sont enregistrés ensemble ; si une vérification échoue (incident résolu, prestataire inexistant), rien n'est modifié.
- `IncidentService.resoudre` : le statut `RESOLU`, la date de résolution et le nouvel état de l'équipement sont enregistrés ensemble.
- `EvaluationService.evaluer` : la vérification « pas encore évalué » et l'insertion se font dans la même transaction.
- Les lectures (`consulterDetail`, `estModifiable`, `estAffectable`, `estEvaluable`, `incidentsModifiables`, historique, prestataires avec leur note, tableau de bord, carte du réseau) sont en `readOnly = true`.

## LOT ALPHA
### Fonctionnalité transverse 1 : Cartographie du réseau
- Besoin : voir d'un coup d'œil la situation du réseau et ouvrir, depuis la carte, les équipements et les incidents d'une station.
- Périmètre : page `/carte`, ouverte à tous les profils connectés. Un marqueur par station (coordonnées déjà en base), coloré selon sa situation, c'est-à-dire l'état le plus grave de ses équipements. Le clic ouvre un panneau latéral : équipements triés du plus grave au moins grave, avec leur état et leurs incidents actifs, chacun menant à sa page. Les données ne contiennent pas le tracé des lignes de métro : la carte montre les stations, pas les lignes.
- Choix :
  - Leaflet 1.9.4 (chargé depuis unpkg avec contrôle d'intégrité) et fond OpenStreetMap : bibliothèque légère, sans clé d'API. Le fond est désaturé pour que seules les situations restent en couleur.
  - La page est rendue par Thymeleaf : la liste des stations porte les coordonnées en attributs `data-*`, et `carte.js` crée les marqueurs à partir de cette liste. Pas de route JSON à ajouter (l'API exige un token, les pages une session), et Thymeleaf échappe les noms.
  - Sans JS ou sans Leaflet, la liste et les fiches restent utilisables (ancres, message à la place de la carte). Une station sans coordonnées reste dans la liste, signalée « Non localisée ».
  - L'ancre garde la station choisie : le bouton « Précédent » depuis un équipement revient sur sa fiche.
- Ce qui fonctionne : carte, légende avec le nombre de stations par situation, panneau, navigation clavier par la liste (Échap referme la fiche), affichage mobile. Vérifié dans un navigateur avec des données fictives, et par `CarteServiceTest` et `CarteViewControllerTest`.
- Difficultés :
  - Une carte créée alors que son cadre n'a pas encore de taille se cadre mal (tous les marqueurs au même point) : un `ResizeObserver` la recadre dès que le cadre a des dimensions.
  - Les étiquettes se recouvraient quand les stations sont proches : celles qui gêneraient sont masquées jusqu'à ce qu'on zoome.
  - Le nom `.carte` était déjà pris par la carte d'interface : les classes de cette page utilisent le préfixe `carto-`.
- Reste à faire : le fond de carte et Leaflet exigent un accès à Internet ; héberger Leaflet dans le projet le rendrait indépendant d'unpkg. Tracé des lignes si le modèle de données l'intègre un jour.
### Fonctionnalité transverse 2 : Qualité renforcée

## BILAN
- Contribution de chacun : ...
- Plus grande difficulté et comment elle a été surmontée : ...
- Fonctionnalités non réalisées et pourquoi : ...
- Trois points à retenir du cours et du projet : ...