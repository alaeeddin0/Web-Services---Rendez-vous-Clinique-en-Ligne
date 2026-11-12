package com.clinique.service_notifications.entity;

import java.time.LocalDateTime;

import com.clinique.service_notifications.model.TypeNotification;
import jakarta.persistence.*;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer rendezVousId;

    @Column(nullable = false)
    private Integer patientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeNotification type;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private LocalDateTime dateEnvoi;

    public Notification() {
    }

    public Notification(Integer rendezVousId, Integer patientId,
                        TypeNotification type, String message) {
        this.rendezVousId = rendezVousId;
        this.patientId = patientId;
        this.type = type;
        this.message = message;
        this.dateEnvoi = LocalDateTime.now();
    }

    @PrePersist
    public void beforeInsert() {
        if (dateEnvoi == null) {
            dateEnvoi = LocalDateTime.now();
        }
    }

    public Integer getId() { return id; }
    public Integer getRendezVousId() { return rendezVousId; }
    public Integer getPatientId() { return patientId; }
    public TypeNotification getType() { return type; }
    public String getMessage() { return message; }
    public LocalDateTime getDateEnvoi() { return dateEnvoi; }

    public void setId(Integer id) { this.id = id; }
    public void setRendezVousId(Integer rendezVousId) { this.rendezVousId = rendezVousId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }
    public void setType(TypeNotification type) { this.type = type; }
    public void setMessage(String message) { this.message = message; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }
}
