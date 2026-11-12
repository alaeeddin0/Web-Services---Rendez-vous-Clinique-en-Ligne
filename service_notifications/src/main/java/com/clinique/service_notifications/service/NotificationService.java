package com.clinique.service_notifications.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinique.service_notifications.dto.NotificationRequest;
import com.clinique.service_notifications.entity.Notification;
import com.clinique.service_notifications.exception.ResourceNotFoundException;
import com.clinique.service_notifications.repository.NotificationRepository;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    // Créer et enregistrer une notification
    public Notification envoyerNotification(NotificationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La notification ne peut pas être null");
        }

        Notification notification = new Notification(
                request.getRendezVousId(),
                request.getPatientId(),
                request.getType(),
                request.getMessage().trim()
        );
        return repository.save(notification);
    }

    // Consulter toutes les notifications d'un patient
    @Transactional(readOnly = true)
    public List<Notification> consulterNotificationsPatient(Integer patientId) {
        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("patientId doit être positif");
        }
        return repository.findByPatientIdOrderByDateEnvoiDesc(patientId);
    }

    // Consulter une notification par son identifiant
    @Transactional(readOnly = true)
    public Notification rechercherNotification(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification introuvable avec id: " + id));
    }

    // Consulter les notifications liées à un rendez-vous
    @Transactional(readOnly = true)
    public List<Notification> consulterNotificationsRendezVous(Integer rendezVousId) {
        if (rendezVousId == null || rendezVousId <= 0) {
            throw new IllegalArgumentException("rendezVousId doit être positif");
        }
        return repository.findByRendezVousIdOrderByDateEnvoiDesc(rendezVousId);
    }

    // Supprimer une notification par son identifiant
    public boolean removeNotification(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id doit être positif");
        }
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    @Transactional(readOnly = true)
    public List<Notification> consulterToutesNotifications() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public long nombreNotifications() {
        return repository.count();
    }
}
