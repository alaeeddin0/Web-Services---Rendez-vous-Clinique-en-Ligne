package com.example.service_creneaux.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreneauRequest(

        @NotNull(message = "L'identifiant du médecin est obligatoire") Long medecinId,

        @NotNull(message = "La date est obligatoire") @FutureOrPresent(message = "La date ne peut pas être dans le passé") LocalDate date,

        @NotNull(message = "L'heure de début est obligatoire") LocalTime heureDebut,

        @NotNull(message = "L'heure de fin est obligatoire") LocalTime heureFin) {
}