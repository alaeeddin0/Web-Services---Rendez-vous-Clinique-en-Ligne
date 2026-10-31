package com.example.service_creneaux.dto;

import com.example.service_creneaux.model.Creneau;
import com.example.service_creneaux.model.StatutCreneau;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreneauResponse(
        Long id,
        Long medecinId,
        LocalDate date,
        LocalTime heureDebut,
        LocalTime heureFin,
        StatutCreneau statut) {

    public static CreneauResponse from(Creneau creneau) {
        return new CreneauResponse(
                creneau.getId(),
                creneau.getMedecinId(),
                creneau.getDate(),
                creneau.getHeureDebut(),
                creneau.getHeureFin(),
                creneau.getStatut());
    }
}