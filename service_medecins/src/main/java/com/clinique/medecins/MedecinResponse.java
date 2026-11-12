package com.clinique.medecins;
public record MedecinResponse(Long id, String nom, String prenom, String email, String role, String specialite) {
 static MedecinResponse from(Medecin m) {
  return new MedecinResponse(m.id,m.nom,m.prenom,m.email,m.role,m.specialite);
 }
}
