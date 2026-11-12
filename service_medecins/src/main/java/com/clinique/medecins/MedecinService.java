package com.clinique.medecins;

import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class MedecinService {
    private final MedecinRepository repository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public MedecinService(MedecinRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<MedecinResponse> list(String specialite) {
        var doctors = specialite == null || specialite.isBlank()
                ? repository.findAllByOrderByNomAscPrenomAsc()
                : repository.findBySpecialiteIgnoreCaseOrderByNomAscPrenomAsc(specialite.strip());
        return doctors.stream().map(MedecinResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public MedecinResponse get(Long id) {
        return MedecinResponse.from(find(id));
    }

    public MedecinResponse create(MedecinRequest request) {
        String email = normalize(request.email());
        if (repository.existsByEmailIgnoreCase(email)) {
            throw conflict();
        }
        // BCrypt accepts at most 72 bytes, including multi-byte Unicode characters.
        if (request.motDePasse().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le mot de passe dépasse 72 octets UTF-8.");
        var doctor = new Medecin();
        doctor.nom = request.nom().strip();
        doctor.prenom = request.prenom().strip();
        doctor.email = email;
        doctor.specialite = request.specialite().strip();
        doctor.role = "MEDECIN";
        doctor.motDePasse = encoder.encode(request.motDePasse());
        return MedecinResponse.from(repository.saveAndFlush(doctor));
    }

    public MedecinResponse update(Long id, MedecinUpdateRequest request) {
        var doctor = find(id);
        String email = normalize(request.email());
        if (repository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw conflict();
        }
        doctor.nom = request.nom().strip();
        doctor.prenom = request.prenom().strip();
        doctor.email = email;
        doctor.specialite = request.specialite().strip();
        return MedecinResponse.from(repository.saveAndFlush(doctor));
    }

    private Medecin find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Médecin introuvable."));
    }

    private static String normalize(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private static ResponseStatusException conflict() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Cet email est déjà utilisé.");
    }
}
