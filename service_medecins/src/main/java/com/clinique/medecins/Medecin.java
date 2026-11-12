package com.clinique.medecins;
import jakarta.persistence.*;
@Entity
@Table(name="medecins")
public class Medecin extends User {
 @Column(nullable=false, length=100) String specialite;
 protected Medecin() {}
}
