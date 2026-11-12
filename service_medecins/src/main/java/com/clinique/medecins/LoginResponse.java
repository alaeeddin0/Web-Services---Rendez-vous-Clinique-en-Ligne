package com.clinique.medecins;

public record LoginResponse(Long id, String nom, String prenom, String email, String role) {
    static LoginResponse from(User user) {
        return new LoginResponse(user.id, user.nom, user.prenom, user.email, user.role);
    }
}
