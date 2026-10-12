# Journal — Opsis (ALTN72, 2026-27)

**Groupe :** Thomas GUIBERT, Roméo FROGER

## README
- Prérequis : Java 25, Maven ..., base ...
- Initialiser la base : `db/create.sql` (ou procédure ...)
- Lancer : `mvn spring-boot:run`
- Application déployée : https://...
- Comptes de test : profil 1 : login / mot de passe ; profil 2 : ...
- Limites connues : ...

## ARCHITECTURE
- Dépôt : https://...
- Schéma : ![architecture](docs/architecture.png)

## TESTS
- Stratégie : ...
- Commande : `mvn verify`
- Couvert : ... | Non couvert : ... (pourquoi)

## CHOIX TECHNIQUES
- Droits par profil :
- | N°    |            Fonctionnalité             | Profil 1: Responsable d’exploitation | Profil 2: Agent |
  |:------|:-------------------------------------:|-------------------------------------:|----------------:|
  | F2–F3 |    Consulter équipements et fiches    |                                  OUI |             OUI |
  | F4    |         Déclarer un incident          |                                  NON |             OUI |
  | F5–F6 |   Consulter / modifier un incident    |                                  OUI |   PARTIELLEMENT |
  | F7    | Affecter ou réaffecter un prestataire |                                  OUI |             NON |
  | F9    |      Déclarer un incident résolu      |                                  OUI |             NON |
  | F10   |        Évaluer un prestataire         |                                  OUI |             NON |
  | F11   |            Tableau de bord            |                                  OUI |             NON |

- Exceptions : @ControllerAdvice ou ResponseStatusException ? Pourquoi ?
- SOLID : principe(s) appliqué(s) (où, pourquoi) ; principes écartés (pourquoi)
- Requête SQL native : où ? analyse critique
- @Transactional : où ? pourquoi ?

## LOT ALPHA
### Fonctionnalité transverse 1 : ...
- Besoin · périmètre envisagé · choix et essais · ce qui fonctionne · difficultés · reste à faire
### Fonctionnalité transverse 2 : ...

## BILAN
- Contribution de chacun : ...
- Plus grande difficulté et comment elle a été surmontée : ...
- Fonctionnalités non réalisées et pourquoi : ...
- Trois points à retenir du cours et du projet : ...