# Clinique Plus

> Une plateforme de prise de rendez-vous clinique, construite comme une application distribuée : le patient trouve un médecin, réserve un créneau et reçoit le suivi de sa demande.

![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)
![Java](https://img.shields.io/badge/Java-21%20%7C%2017-ED8B00?logo=openjdk&logoColor=white)
![Laravel](https://img.shields.io/badge/Laravel-12-FF2D20?logo=laravel&logoColor=white)
![React](https://img.shields.io/badge/React-Vite-61DAFB?logo=react&logoColor=111)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)

## À propos

**Clinique Plus** permet de gérer le parcours de rendez-vous médical de bout en bout :

- un patient crée son compte et se connecte ;
- un médecin crée ses disponibilités ;
- le patient choisit un médecin et réserve un créneau libre ;
- le médecin confirme ou refuse la demande ;
- le patient reçoit une notification et consulte l’état de ses rendez-vous.

L’application est organisée en microservices. Chaque service porte une responsabilité métier claire, mais tous utilisent la même base PostgreSQL de développement.

## Architecture

```text
Navigateur
    │
    ▼
Frontend React + Nginx (http://localhost:3000)
    ├── Service Médecins / Authentification     :8081
    ├── Service Créneaux                        :8082
    ├── Service Rendez-vous (Laravel)           :8002
    └── Service Notifications                   :8083
                         │
                         ▼
                  PostgreSQL :5433
```

| Service | Responsabilité | Technologie | Port hôte |
|---|---|---|---:|
| `frontend` | Interface web, navigation et appels API | React, Vite, Nginx | 3000 |
| `service_medecins` | Médecins, patients, inscription et connexion | Spring Boot / Java 21 | 8081 |
| `service_creneaux` | Création et réservation des disponibilités | Spring Boot / Java 21 | 8082 |
| `service-rendez-vous/backend` | Demandes, confirmation, refus et annulation | Laravel / PHP | 8002 |
| `service_notifications` | Historique des notifications | Spring Boot / Java 17 | 8083 |
| `db` | Base de données commune de développement | PostgreSQL 16 | 5433 |

## Règles métier importantes

- Un créneau appartient à un seul médecin.
- Seul un créneau `LIBRE` peut être réservé.
- Un rendez-vous réserve le créneau auprès du service Créneaux avant son enregistrement.
- Lors d’un refus ou d’une annulation, le créneau est libéré.
- Le médecin doit exister avant la création d’un créneau.
- Les liens entre services sont réalisés par des identifiants (`patientId`, `medecinId`, `creneauId`, `rendezVousId`).
- La base de données est commune en développement, mais chaque service reste responsable de ses propres tables et de sa logique métier.

## Prérequis

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) démarré ;
- Git, si vous souhaitez cloner le projet ;
- un navigateur récent.

> Docker est suffisant pour lancer l’application complète. Java, PHP, Node.js et Maven ne sont requis que pour travailler sur un service localement.

## Démarrage rapide

Depuis la racine du projet :

```powershell
cd "C:\Users\fhac8\OneDrive\Desktop\rendez-vous clinique"
docker compose up --build -d
```

Vérifier l’état des conteneurs :

```powershell
docker compose ps
```

Ouvrir ensuite :

- Application : [http://localhost:3000](http://localhost:3000)
- API Médecins : [http://localhost:8081/medecins](http://localhost:8081/medecins)
- API Créneaux : [http://localhost:8082/creneaux](http://localhost:8082/creneaux)
- API Rendez-vous : [http://localhost:8002/api/rendezvous](http://localhost:8002/api/rendezvous)
- API Notifications : [http://localhost:8083/api/notifications](http://localhost:8083/api/notifications)

Pour suivre les journaux :

```powershell
docker compose logs -f
```

Pour arrêter les services **sans supprimer les données** :

```powershell
docker compose down
```

Pour réinitialiser complètement la base (attention : données supprimées) :

```powershell
docker compose down -v
docker compose up --build -d
```

## Parcours de démonstration

1. Ouvrir `http://localhost:3000`.
2. Créer un compte **Médecin**, puis se connecter.
3. Dans l’espace médecin, créer un créneau avec une date et des heures valides.
4. Se déconnecter puis créer un compte **Patient**.
5. Se connecter en patient, choisir le médecin et réserver un créneau libre.
6. Revenir au compte médecin, puis confirmer ou refuser la demande.
7. Revenir au compte patient : le rendez-vous et la notification sont visibles dans son espace.

## Authentification

L’authentification est fournie par `service_medecins`.

| Méthode | Endpoint | Usage |
|---|---|---|
| `POST` | `/auth/register/patient` | Inscription d’un patient |
| `POST` | `/auth/register/medecin` | Inscription d’un médecin |
| `POST` | `/auth/login` | Connexion patient ou médecin |
| `GET` | `/patients/{id}` | Récupération d’un patient |

Les mots de passe sont hachés avec **BCrypt** et un email ne peut être utilisé qu’une seule fois. Le frontend conserve actuellement la session dans `localStorage` afin de distinguer les deux espaces. Une évolution de production consisterait à protéger les API avec des jetons JWT.

Exemple de connexion :

```json
POST /auth/login
{
  "email": "patient@clinique.ma",
  "motDePasse": "motdepasse123"
}
```

## APIs métier principales

| Domaine | Méthode | Endpoint | Description |
|---|---|---|---|
| Médecins | `GET` | `/medecins` | Liste des médecins |
| Médecins | `GET` | `/medecins/{id}` | Détail d’un médecin |
| Créneaux | `POST` | `/creneaux` | Créer un créneau |
| Créneaux | `GET` | `/creneaux?medecinId={id}&statut=LIBRE` | Créneaux disponibles d’un médecin |
| Créneaux | `PUT` | `/creneaux/{id}/reserver` | Réserver un créneau |
| Créneaux | `PUT` | `/creneaux/{id}/liberer` | Libérer un créneau |
| Rendez-vous | `POST` | `/api/rendezvous` | Créer une demande |
| Rendez-vous | `PUT` | `/api/rendezvous/{id}/confirmer` | Confirmer une demande |
| Rendez-vous | `PUT` | `/api/rendezvous/{id}/refuser` | Refuser une demande |
| Rendez-vous | `PUT` | `/api/rendezvous/{id}/annuler` | Annuler une demande |
| Notifications | `GET` | `/api/notifications/patient/{patientId}` | Notifications d’un patient |

Exemple de création d’un créneau :

```json
POST /creneaux
{
  "medecinId": 1,
  "date": "2026-12-01",
  "heureDebut": "09:00",
  "heureFin": "09:30"
}
```

Exemple de demande de rendez-vous :

```json
POST /api/rendezvous
{
  "creneau_id": 1,
  "medecin_id": 1,
  "patient_id": 2
}
```

## Développement local

Le frontend communique avec les APIs via Nginx, avec les préfixes suivants :

| Préfixe frontend | Service cible |
|---|---|
| `/backend/auth` | Service Médecins |
| `/backend/medecins` | Service Médecins |
| `/backend/creneaux` | Service Créneaux |
| `/backend/rendezvous` | Service Rendez-vous |
| `/backend/notifications` | Service Notifications |

Pour reconstruire uniquement le frontend après une modification :

```powershell
docker compose up --build -d frontend
```

## Arborescence

```text
rendez-vous clinique/
├── frontend/                         # Interface React
├── service_medecins/                 # Comptes, médecins et patients
├── service_creneaux/                 # Disponibilités des médecins
├── service-rendez-vous/backend/      # Gestion des rendez-vous Laravel
├── service_notifications/            # Notifications
└── docker-compose.yml                # Lancement de toute l'application
```

## Équipe et intégration

Avant toute intégration, chaque membre doit :

1. conserver les contrats d’API définis ci-dessus ;
2. ne pas modifier les tables appartenant à un autre service ;
3. tester son service contre PostgreSQL et Docker Compose ;
4. informer l’équipe avant de changer un endpoint, une valeur de statut ou un format JSON ;
5. vérifier le parcours complet après fusion : médecin → créneau → patient → rendez-vous → notification.

---

Projet académique — **Clinique Plus**
