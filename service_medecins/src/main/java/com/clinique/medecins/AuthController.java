package com.clinique.medecins;

import java.net.URI;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service) { this.service = service; }

    @PostMapping("/auth/register/patient")
    public ResponseEntity<PatientResponse> patient(@Valid @RequestBody PatientRequest request) {
        PatientResponse created = service.registerPatient(request);
        return ResponseEntity.created(URI.create("/patients/" + created.id())).body(created);
    }
    @PostMapping("/auth/register/medecin")
    public ResponseEntity<MedecinResponse> medecin(@Valid @RequestBody MedecinRequest request) {
        MedecinResponse created = service.registerMedecin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) { return service.login(request); }
    @GetMapping("/patients/{id}")
    public PatientResponse patient(@PathVariable Long id) { return service.patient(id); }
}
