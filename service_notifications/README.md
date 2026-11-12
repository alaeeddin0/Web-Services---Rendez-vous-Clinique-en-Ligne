# Service Notifications - Clinique

Microservice REST Spring Boot construit à partir du diagramme de classes fourni.

## Technologies
- Java 17
- Spring Boot 3.4.5
- Spring Web
- Spring Data JPA
- H2 Database (en mémoire)
- JUnit 5 + Mockito

## Prérequis
- JDK 17 ou supérieur
- Maven 3.6+ (ou Maven Wrapper si vous l'ajoutez)

## Lancer dans VS Code
1. Ouvrir le dossier `service_notifications` dans VS Code.
2. Vérifier que Java 17 est installé : `java -version`.
3. Ouvrir Terminal > New Terminal.
4. Lancer : `mvn spring-boot:run`

Si `mvn` n'est pas reconnu, installer Maven ou générer/ajouter le Maven Wrapper depuis un environnement Maven. Vous pouvez aussi installer les extensions VS Code "Extension Pack for Java" et "Spring Boot Extension Pack".

## Lancer les tests
```bash
mvn test
```

Les tests unitaires utilisent Mockito : aucun serveur ni base de données réelle n'est nécessaire.

## URL
- API : `http://localhost:8083/api/notifications`
- Console H2 : `http://localhost:8083/h2-console`
  - JDBC URL: `jdbc:h2:mem:notificationsdb`
  - User: `sa`
  - Password: laisser vide

La base H2 est en mémoire : les données sont effacées à l'arrêt du serveur.

## Endpoints

| Méthode | URL | Description |
|---|---|---|
| POST | `/api/notifications` | Créer une notification |
| GET | `/api/notifications` | Lister toutes les notifications |
| GET | `/api/notifications/{id}` | Chercher par ID |
| GET | `/api/notifications/patient/{patientId}` | Notifications d'un patient |
| GET | `/api/notifications/rendez-vous/{rendezVousId}` | Notifications d'un rendez-vous |
| GET | `/api/notifications/count` | Nombre total |
| DELETE | `/api/notifications/{id}` | Supprimer par ID |

## Exemple POST
URL: `POST http://localhost:8083/api/notifications`

Header: `Content-Type: application/json`

Body:
```json
{
  "rendezVousId": 101,
  "patientId": 10,
  "type": "CONFIRMATION",
  "message": "Votre rendez-vous est confirmé"
}
```

Types acceptés: `CONFIRMATION`, `REFUS`, `RAPPEL`.

## Limites liées au diagramme
Le diagramme montre des associations par identifiants (`patientId`, `rendezVousId`) entre microservices. Ce service stocke ces identifiants, mais ne crée pas les entités Patient ou RendezVous localement et ne vérifie pas leur existence auprès des autres microservices. L'envoi est ici l'enregistrement d'une notification en base, pas un vrai SMS ou e-mail.
