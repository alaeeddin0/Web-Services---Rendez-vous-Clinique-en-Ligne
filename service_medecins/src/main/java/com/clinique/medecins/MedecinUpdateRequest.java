package com.clinique.medecins;
import jakarta.validation.constraints.*;
public record MedecinUpdateRequest(
 @NotBlank @Size(max=100) String nom,
 @NotBlank @Size(max=100) String prenom,
 @NotBlank @Email @Size(max=254) String email,
 @NotBlank @Size(max=100) String specialite
) {}
