package com.clinique.service_notifications.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.clinique.service_notifications.dto.NotificationRequest;
import com.clinique.service_notifications.entity.Notification;
import com.clinique.service_notifications.service.NotificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    // POST /api/notifications
    @PostMapping
    public ResponseEntity<Notification> envoyer(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.envoyerNotification(request));
    }

    // GET /api/notifications/patient/10
    @GetMapping("/patient/{patientId}")
    public List<Notification> consulterParPatient(@PathVariable Integer patientId) {
        return service.consulterNotificationsPatient(patientId);
    }

    // GET /api/notifications/1
    @GetMapping("/{id}")
    public Notification rechercher(@PathVariable Integer id) {
        return service.rechercherNotification(id);
    }

    // GET /api/notifications/rendez-vous/101
    @GetMapping("/rendez-vous/{rendezVousId}")
    public List<Notification> consulterParRendezVous(@PathVariable Integer rendezVousId) {
        return service.consulterNotificationsRendezVous(rendezVousId);
    }

    // DELETE /api/notifications/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Integer id) {
        boolean deleted = service.removeNotification(id);
        return deleted ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // GET /api/notifications
    @GetMapping
    public List<Notification> consulterToutes() {
        return service.consulterToutesNotifications();
    }

    // GET /api/notifications/count
    @GetMapping("/count")
    public long nombre() {
        return service.nombreNotifications();
    }
}
