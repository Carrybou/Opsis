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

