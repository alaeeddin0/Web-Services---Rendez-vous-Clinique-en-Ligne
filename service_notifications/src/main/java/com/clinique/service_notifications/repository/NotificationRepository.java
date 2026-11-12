package com.clinique.service_notifications.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.clinique.service_notifications.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByPatientIdOrderByDateEnvoiDesc(Integer patientId);
    List<Notification> findByRendezVousIdOrderByDateEnvoiDesc(Integer rendezVousId);
}
