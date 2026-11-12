package com.clinique.medecins;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MedecinRepository extends JpaRepository<Medecin,Long> {
 List<Medecin> findBySpecialiteIgnoreCaseOrderByNomAscPrenomAsc(String specialite);
 List<Medecin> findAllByOrderByNomAscPrenomAsc();
 boolean existsByEmailIgnoreCase(String email);
 boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
