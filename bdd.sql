SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS Base_ALTN72
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE Base_ALTN72;

-- ============================================================
-- Suppression des tables existantes (ordre inverse des dépendances)
-- ============================================================
DROP TABLE IF EXISTS evaluation;
DROP TABLE IF EXISTS incident;
DROP TABLE IF EXISTS equipement;
DROP TABLE IF EXISTS prestataire;
DROP TABLE IF EXISTS utilisateur;
DROP TABLE IF EXISTS station;

-- ============================================================
-- Création des tables
-- ============================================================

-- Table Station
CREATE TABLE station (
                         identifiant BIGINT AUTO_INCREMENT PRIMARY KEY,
                         nom VARCHAR(255) NOT NULL,
                         latitude DECIMAL(10, 8), -- Précision adaptée pour des coordonnées GPS
                         longitude DECIMAL(11, 8)
);

-- Table Utilisateur
CREATE TABLE utilisateur (
                             identifiant BIGINT AUTO_INCREMENT PRIMARY KEY,
                             identifiant_connexion VARCHAR(100) NOT NULL UNIQUE,
                             mot_de_passe VARCHAR(255) NOT NULL, -- Assez long pour stocker le hash (BCrypt, Argon2...)
                             prenom VARCHAR(100) NOT NULL,
                             profil VARCHAR(50) NOT NULL
);

-- Table Prestataire
CREATE TABLE prestataire (
                             identifiant BIGINT AUTO_INCREMENT PRIMARY KEY,
                             nom VARCHAR(255) NOT NULL,
                             contact VARCHAR(255) NOT NULL
);

-- Table Equipement
CREATE TABLE equipement (
                            identifiant BIGINT AUTO_INCREMENT PRIMARY KEY,
                            reference_unique VARCHAR(100) NOT NULL UNIQUE,
                            libelle_description VARCHAR(255) NOT NULL,
                            type VARCHAR(100) NOT NULL,
                            etat_fonctionnement VARCHAR(50) NOT NULL,
                            id_station BIGINT NOT NULL,
                            CONSTRAINT fk_equipement_station FOREIGN KEY (id_station) REFERENCES station(identifiant) ON DELETE CASCADE
);

-- Table Incident
CREATE TABLE incident (
                          identifiant BIGINT AUTO_INCREMENT PRIMARY KEY,
                          description TEXT NOT NULL,
                          date_heure_ouverture DATETIME NOT NULL,
                          statut VARCHAR(50) NOT NULL,
                          date_heure_resolution DATETIME DEFAULT NULL,
                          id_equipement BIGINT NOT NULL,
                          id_prestataire BIGINT DEFAULT NULL,
                          id_utilisateur BIGINT NOT NULL,
                          CONSTRAINT fk_incident_equipement FOREIGN KEY (id_equipement) REFERENCES equipement(identifiant) ON DELETE CASCADE,
                          CONSTRAINT fk_incident_prestataire FOREIGN KEY (id_prestataire) REFERENCES prestataire(identifiant) ON DELETE SET NULL,
                          CONSTRAINT fk_incident_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(identifiant) ON DELETE CASCADE
);

-- Table Evaluation
CREATE TABLE evaluation (
                            identifiant BIGINT AUTO_INCREMENT PRIMARY KEY,
                            note INT NOT NULL CHECK (note >= 1 AND note <= 5), -- Contrainte d'intégrité pour la note de 1 à 5
                            commentaire TEXT,
                            date DATE NOT NULL,
                            id_incident BIGINT NOT NULL UNIQUE, -- UNIQUE garantit qu'il n'y a qu'une seule évaluation par incident
                            CONSTRAINT fk_evaluation_incident FOREIGN KEY (id_incident) REFERENCES incident(identifiant) ON DELETE CASCADE
);

USE Base_ALTN72;

INSERT INTO station (identifiant, nom, latitude, longitude) VALUES
                                                                (1, 'Châtelet', 48.85837200, 2.34706100),
                                                                (2, 'Gare de Lyon', 48.84443000, 2.37383200),
                                                                (3, 'République', 48.86749800, 2.36384200),
                                                                (4, 'La Défense', 48.89197800, 2.23796600),
                                                                (5, 'Montparnasse', 48.84211000, 2.32128000);

INSERT INTO utilisateur (identifiant, identifiant_connexion, mot_de_passe, prenom, profil) VALUES
                                                                                               (1, 'admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BW', 'Alice', 'ADMIN'),
                                                                                               (2, 'tech.martin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BW', 'Lucas', 'TECHNICIEN'),
                                                                                               (3, 'tech.dubois', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BW', 'Inès', 'TECHNICIEN'),
                                                                                               (4, 'agent.bernard', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BW', 'Hugo', 'AGENT'),
                                                                                               (5, 'agent.petit', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BW', 'Léa', 'AGENT'),
                                                                                               (6, 'agent.moreau', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BW', 'Nathan', 'AGENT');

INSERT INTO prestataire (identifiant, nom, contact) VALUES
                                                        (1, 'AscenTech Services', 'astreinte@ascentech-exemple.fr'),
                                                        (2, 'Mobilis Maintenance', '01 45 67 89 10'),
                                                        (3, 'BilletPro Support', 'support@billetpro-exemple.fr'),
                                                        (4, 'ElecFlux', 'interventions@elecflux-exemple.fr'),
                                                        (5, 'Nettoyage Express', '01 98 76 54 32');

INSERT INTO equipement (identifiant, reference_unique, libelle_description, type, etat_fonctionnement, id_station) VALUES
                                                                                                                       (1, 'ESC-CHT-001', 'Escalator sortie rue de Rivoli', 'ESCALATOR', 'FONCTIONNEL', 1),
                                                                                                                       (2, 'ASC-CHT-001', 'Ascenseur accès quai ligne 1', 'ASCENSEUR', 'EN_PANNE', 1),
                                                                                                                       (3, 'DIS-CHT-001', 'Distributeur de titres hall principal', 'DISTRIBUTEUR', 'FONCTIONNEL', 1),
                                                                                                                       (4, 'ESC-GDL-001', 'Escalator accès RER A', 'ESCALATOR', 'EN_MAINTENANCE', 2),
                                                                                                                       (5, 'VAL-GDL-001', 'Valideur portique nord', 'VALIDEUR', 'FONCTIONNEL', 2),
                                                                                                                       (6, 'ECR-GDL-001', 'Écran information voyageurs hall 1', 'ECRAN', 'EN_PANNE', 2),
                                                                                                                       (7, 'ASC-REP-001', 'Ascenseur PMR sortie 1', 'ASCENSEUR', 'FONCTIONNEL', 3),
                                                                                                                       (8, 'DIS-REP-001', 'Distributeur de titres sortie 3', 'DISTRIBUTEUR', 'EN_PANNE', 3),
                                                                                                                       (9, 'ESC-LDF-001', 'Escalator parvis', 'ESCALATOR', 'FONCTIONNEL', 4),
                                                                                                                       (10, 'VAL-LDF-001', 'Valideur portique sud', 'VALIDEUR', 'FONCTIONNEL', 4),
                                                                                                                       (11, 'ECR-LDF-001', 'Écran quai ligne 1', 'ECRAN', 'FONCTIONNEL', 4),
                                                                                                                       (12, 'ASC-MTP-001', 'Ascenseur accès grandes lignes', 'ASCENSEUR', 'FONCTIONNEL', 5),
                                                                                                                       (13, 'DIS-MTP-001', 'Distributeur de titres hall 2', 'DISTRIBUTEUR', 'EN_MAINTENANCE', 5),
                                                                                                                       (14, 'VAL-MTP-001', 'Valideur portique est', 'VALIDEUR', 'FONCTIONNEL', 5);

INSERT INTO incident (identifiant, description, date_heure_ouverture, statut, date_heure_resolution, id_equipement, id_prestataire, id_utilisateur) VALUES
                                                                                                                                                        (1, 'Ascenseur bloqué entre deux niveaux, aucun usager à l''intérieur', '2026-10-10 08:15:00', 'EN_COURS', NULL, 2, 1, 4),
                                                                                                                                                        (2, 'Écran éteint, aucun affichage des prochains départs', '2026-10-13 07:40:00', 'OUVERT', NULL, 6, NULL, 5),
                                                                                                                                                        (3, 'Le distributeur refuse les paiements par carte bancaire', '2026-10-12 18:20:00', 'EN_COURS', NULL, 8, 3, 6),
                                                                                                                                                        (4, 'Bruit anormal puis arrêt complet de l''escalator', '2026-10-11 09:00:00', 'EN_COURS', NULL, 4, 2, 4),
                                                                                                                                                        (5, 'Écran tactile du distributeur ne répond plus', '2026-10-14 06:50:00', 'EN_COURS', NULL, 13, 3, 5),
                                                                                                                                                        (6, 'Marche cassée en bas de l''escalator', '2026-09-02 07:30:00', 'RESOLU', '2026-09-03 14:00:00', 1, 2, 4),
                                                                                                                                                        (7, 'Le valideur n''accepte plus les passes sans contact', '2026-09-10 17:45:00', 'RESOLU', '2026-09-10 21:10:00', 5, 4, 5),
                                                                                                                                                        (8, 'Bouton d''appel du rez-de-chaussée hors service', '2026-09-15 10:20:00', 'RESOLU', '2026-09-16 11:00:00', 7, 1, 6),
                                                                                                                                                        (9, 'Arrêt d''urgence déclenché, redémarrage impossible', '2026-09-20 08:05:00', 'RESOLU', '2026-09-20 09:30:00', 9, 2, 4),
                                                                                                                                                        (10, 'Affichage figé sur une information périmée', '2026-09-25 12:00:00', 'RESOLU', '2026-09-26 08:45:00', 11, 4, 5),
                                                                                                                                                        (11, 'Les portes se rouvrent en permanence', '2026-10-01 16:30:00', 'RESOLU', '2026-10-03 10:00:00', 12, 1, 6),
                                                                                                                                                        (12, 'Lecteur de carte intermittent', '2026-10-05 07:10:00', 'RESOLU', '2026-10-05 15:20:00', 10, 4, 2),
                                                                                                                                                        (13, 'Signalement ouvert sans prestataire affecté', '2026-10-14 09:30:00', 'OUVERT', NULL, 3, NULL, 3);

INSERT INTO evaluation (identifiant, note, commentaire, date, id_incident) VALUES
                                                                               (1, 4, 'Intervention rapide, marche remplacée dans la journée', '2026-09-04', 6),
                                                                               (2, 5, 'Réparé le soir même, parfait', '2026-09-11', 7),
                                                                               (3, 3, 'Délai d''intervention un peu long', '2026-09-17', 8),
                                                                               (4, 5, 'Remise en service en moins de deux heures', '2026-09-21', 9),
                                                                               (5, 2, 'Le problème est réapparu deux jours plus tard', '2026-09-28', 10);
