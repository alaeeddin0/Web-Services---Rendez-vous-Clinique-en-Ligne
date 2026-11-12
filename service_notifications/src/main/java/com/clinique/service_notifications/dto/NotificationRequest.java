package com.clinique.service_notifications.dto;

import com.clinique.service_notifications.model.TypeNotification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class NotificationRequest {

    @NotNull(message = "rendezVousId est obligatoire")
    @Positive(message = "rendezVousId doit être positif")
    private Integer rendezVousId;

    @NotNull(message = "patientId est obligatoire")
    @Positive(message = "patientId doit être positif")
    private Integer patientId;

    @NotNull(message = "type est obligatoire")
    private TypeNotification type;

    @NotBlank(message = "message est obligatoire")
    private String message;

    public Integer getRendezVousId() { return rendezVousId; }
    public Integer getPatientId() { return patientId; }
    public TypeNotification getType() { return type; }
    public String getMessage() { return message; }

    public void setRendezVousId(Integer rendezVousId) { this.rendezVousId = rendezVousId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }
    public void setType(TypeNotification type) { this.type = type; }
    public void setMessage(String message) { this.message = message; }
}
