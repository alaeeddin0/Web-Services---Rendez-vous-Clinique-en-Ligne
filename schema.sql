-- Création des types énumérés
CREATE TYPE statut_creneau_enum AS ENUM ('LIBRE', 'RESERVE', 'ANNULE');
CREATE TYPE statut_rdv_enum AS ENUM ('EN_ATTENTE', 'CONFIRME', 'REFUSE', 'ANNULE');
CREATE TYPE type_notification_enum AS ENUM ('CONFIRMATION', 'REFUS', 'RAPPEL');

-- 1. Table Médecins (Service Médecins)
CREATE TABLE medecins (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    specialite VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Table Créneaux (Service Créneaux)
CREATE TABLE creneaux (
    id SERIAL PRIMARY KEY,
    medecin_id INT NOT NULL REFERENCES medecins(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    heure_debut TIME NOT NULL,
    heure_fin TIME NOT NULL,
    statut statut_creneau_enum DEFAULT 'LIBRE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Table Rendez-vous (Ton Service - Service Rendez-vous)
CREATE TABLE rendez_vous (
    id SERIAL PRIMARY KEY,
    creneau_id INT UNIQUE NOT NULL REFERENCES creneaux(id) ON DELETE CASCADE,
    medecin_id INT NOT NULL REFERENCES medecins(id) ON DELETE CASCADE,
    patient_id INT NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut statut_rdv_enum DEFAULT 'EN_ATTENTE'
);

-- 4. Table Notifications (Service Notifications)
CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    rendez_vous_id INT NOT NULL REFERENCES rendez_vous(id) ON DELETE CASCADE,
    patient_id INT NOT NULL,
    type type_notification_enum NOT NULL,
    message TEXT NOT NULL,
    date_envoi TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);