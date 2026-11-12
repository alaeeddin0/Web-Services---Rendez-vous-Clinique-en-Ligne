package com.clinique.medecins;
import jakarta.persistence.*;
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
 Long id;
 @Column(nullable=false, length=100) String nom;
 @Column(nullable=false, length=100) String prenom;
 @Column(nullable=false, unique=true, length=254) String email;
 @Column(name="mot_de_passe", nullable=false, length=100) String motDePasse;
 @Column(nullable=false, length=20) String role;
 protected User() {}
}
