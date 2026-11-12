package com.example.service_creneaux.repository;

import com.example.service_creneaux.model.Creneau;
import com.example.service_creneaux.model.StatutCreneau;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CreneauRepository extends JpaRepository<Creneau, Long> {

    List<Creneau> findByMedecinId(Long medecinId);

    List<Creneau> findByDate(LocalDate date);

    List<Creneau> findByStatut(StatutCreneau statut);

    boolean existsByMedecinIdAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
            Long medecinId,
            LocalDate date,
            LocalTime heureFin,
            LocalTime heureDebut);

    @Query("""
            SELECT COUNT(c) > 0
            FROM Creneau c
            WHERE c.medecinId = :medecinId
              AND c.date = :date
              AND c.statut <> :statutAnnule
              AND c.heureDebut < :heureFin
              AND c.heureFin > :heureDebut
              AND (:idExclu IS NULL OR c.id <> :idExclu)
            """)
    boolean existeChevauchement(
            @Param("medecinId") Long medecinId,
            @Param("date") LocalDate date,
            @Param("heureFin") LocalTime heureFin,
            @Param("heureDebut") LocalTime heureDebut,
            @Param("idExclu") Long idExclu,
            @Param("statutAnnule") StatutCreneau statutAnnule);
}
