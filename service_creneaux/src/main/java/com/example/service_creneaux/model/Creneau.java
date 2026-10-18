package com.example.service_creneaux.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "creneau")
public class Creneau {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medecin_id", nullable = false)
    private Long medecinId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @Column(name = "heure_fin", nullable = false)
    private LocalTime heureFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCreneau statut;

    protected Creneau() {
        // Constructeur requis par JPA.
    }

    public Creneau(Long medecinId, LocalDate date,
                   LocalTime heureDebut, LocalTime heureFin) {
        this.medecinId = medecinId;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.statut = StatutCreneau.LIBRE;
    }

    public Long getId() {
        return id;
    }

    public Long getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(Long medecinId) {
        this.medecinId = medecinId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    public StatutCreneau getStatut() {
        return statut;
    }

    public void setStatut(StatutCreneau statut) {
        this.statut = statut;
    }
}