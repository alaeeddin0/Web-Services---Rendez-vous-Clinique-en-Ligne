package com.clinique.medecins;

import java.time.LocalDate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record PatientRequest(
    @NotBlank @Size(max = 100) String nom,
    @NotBlank @Size(max = 100) String prenom,
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Size(min = 8, max = 64) String motDePasse,
    @Size(max = 30) String telephone,
    @Past LocalDate dateNaissance
) {}
