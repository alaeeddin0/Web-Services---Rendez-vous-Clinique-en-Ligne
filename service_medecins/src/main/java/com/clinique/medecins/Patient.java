package com.clinique.medecins;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "patients")
public class Patient extends User {
    @Column(length = 30)
    String telephone;

    @Column(name = "date_naissance")
    LocalDate dateNaissance;

    protected Patient() {}
}
