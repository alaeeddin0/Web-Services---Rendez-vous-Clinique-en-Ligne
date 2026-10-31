package com.example.service_creneaux.controller;

import com.example.service_creneaux.dto.CreneauRequest;
import com.example.service_creneaux.dto.CreneauResponse;
import com.example.service_creneaux.service.CreneauService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.service_creneaux.dto.DisponibiliteResponse;

import java.util.List;

@RestController
@RequestMapping("/creneaux")
public class CreneauController {

    private final CreneauService creneauService;

    public CreneauController(CreneauService creneauService) {
        this.creneauService = creneauService;
    }

    @PostMapping
    public ResponseEntity<CreneauResponse> creer(
            @Valid @RequestBody CreneauRequest request) {
        CreneauResponse creneau = creneauService.creer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creneau);
    }

    @GetMapping
    public ResponseEntity<List<CreneauResponse>> lister() {
        return ResponseEntity.ok(creneauService.lister());
    }
    
    @GetMapping("/{id}/disponibilite")
    public ResponseEntity<DisponibiliteResponse> verifierDisponibilite(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                creneauService.verifierDisponibilite(id));
    }

    @PutMapping("/{id}/reserver")
    public ResponseEntity<CreneauResponse> reserver(
            @PathVariable Long id) {
        return ResponseEntity.ok(creneauService.reserver(id));
    }

    @PutMapping("/{id}/liberer")
    public ResponseEntity<CreneauResponse> liberer(
            @PathVariable Long id) {
        return ResponseEntity.ok(creneauService.liberer(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreneauResponse> trouverParId(
            @PathVariable Long id) {
        return ResponseEntity.ok(creneauService.trouverParId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CreneauResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody CreneauRequest request) {
        return ResponseEntity.ok(creneauService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        creneauService.supprimer(id);

        return ResponseEntity.noContent().build();
    }
}