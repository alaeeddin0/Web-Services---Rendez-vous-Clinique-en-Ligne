package com.clinique.medecins;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/medecins")
public class MedecinController {
 private final MedecinService service;
 public MedecinController(MedecinService service) { this.service=service; }
 @GetMapping
 public List<MedecinResponse> list(@RequestParam(required=false) String specialite) { return service.list(specialite); }
 @GetMapping("/{id}")
 public MedecinResponse get(@PathVariable @Positive Long id) { return service.get(id); }
 @PostMapping
 public ResponseEntity<MedecinResponse> create(@Valid @RequestBody MedecinRequest request) {
  var doctor=service.create(request);
  return ResponseEntity.created(URI.create("/medecins/"+doctor.id())).body(doctor);
 }
 @PutMapping("/{id}")
 public MedecinResponse update(@PathVariable @Positive Long id,@Valid @RequestBody MedecinUpdateRequest request) {
  return service.update(id,request);
 }
}
