package com.clinique.medecins;
import jakarta.validation.constraints.*;
public record MedecinRequest(
 @NotBlank @Size(max=100) String nom,
 @NotBlank @Size(max=100) String prenom,
 @NotBlank @Email @Size(max=254) String email,
 @NotBlank @Size(min=8,max=64) String motDePasse,
 @NotBlank @Size(max=100) String specialite
) {}
