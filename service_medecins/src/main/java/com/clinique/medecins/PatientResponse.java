package com.clinique.medecins;

import java.time.LocalDate;

public record PatientResponse(Long id, String nom, String prenom, String email, String role, String telephone, LocalDate dateNaissance) {
    static PatientResponse from(Patient patient) {
        return new PatientResponse(patient.id, patient.nom, patient.prenom, patient.email, patient.role, patient.telephone, patient.dateNaissance);
    }
}
