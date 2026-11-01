package com.example.service_creneaux.repository;

import com.example.service_creneaux.model.Creneau;
import com.example.service_creneaux.model.StatutCreneau;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CreneauRepository extends JpaRepository<Creneau, Long> {

    List<Creneau> findByMedecinId(Long medecinId);

    List<Creneau> findByDate(LocalDate date);

    List<Creneau> findByStatut(StatutCreneau statut);

    boolean existsByMedecinIdAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
            Long medecinId,
            LocalDate date,
            LocalTime heureFin,
            LocalTime heureDebut);
}