package com.clinique.medecins;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AuthService {
    private final UserRepository users;
    private final PatientRepository patients;
    private final MedecinRepository medecins;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository users, PatientRepository patients, MedecinRepository medecins) {
        this.users = users;
        this.patients = patients;
        this.medecins = medecins;
    }

    public PatientResponse registerPatient(PatientRequest request) {
        String email = normalize(request.email());
        verifyEmailAndPassword(email, request.motDePasse());
        Patient patient = new Patient();
        patient.nom = request.nom().strip(); patient.prenom = request.prenom().strip();
        patient.email = email; patient.motDePasse = encoder.encode(request.motDePasse());
        patient.role = "PATIENT"; patient.telephone = blankToNull(request.telephone());
        patient.dateNaissance = request.dateNaissance();
        return PatientResponse.from(patients.saveAndFlush(patient));
    }

    public MedecinResponse registerMedecin(MedecinRequest request) {
        String email = normalize(request.email());
        verifyEmailAndPassword(email, request.motDePasse());
        Medecin medecin = new Medecin();
        medecin.nom = request.nom().strip(); medecin.prenom = request.prenom().strip();
        medecin.email = email; medecin.motDePasse = encoder.encode(request.motDePasse());
        medecin.role = "MEDECIN"; medecin.specialite = request.specialite().strip();
        return MedecinResponse.from(medecins.saveAndFlush(medecin));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = users.findByEmailIgnoreCase(normalize(request.email()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect."));
        if (!encoder.matches(request.motDePasse(), user.motDePasse)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect.");
        }
        return LoginResponse.from(user);
    }

    @Transactional(readOnly = true)
    public PatientResponse patient(Long id) {
        return PatientResponse.from(patients.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient introuvable.")));
    }

    private void verifyEmailAndPassword(String email, String password) {
        if (users.existsByEmailIgnoreCase(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email est déjà utilisé.");
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le mot de passe dépasse 72 octets UTF-8.");
    }
    private static String normalize(String email) { return email.strip().toLowerCase(Locale.ROOT); }
    private static String blankToNull(String value) { return value == null || value.isBlank() ? null : value.strip(); }
}
