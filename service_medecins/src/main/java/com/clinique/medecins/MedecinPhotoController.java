package com.clinique.medecins;

import jakarta.validation.constraints.Positive;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/medecins/{id}/photo")
public class MedecinPhotoController {
    private final MedecinPhotoService service;
    public MedecinPhotoController(MedecinPhotoService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<byte[]> get(@PathVariable @Positive Long id) {
        var photo = service.get(id);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(photo.contentType()))
            .cacheControl(CacheControl.noCache().cachePrivate())
            .header("X-Content-Type-Options", "nosniff")
            .body(photo.bytes());
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> save(@PathVariable @Positive Long id, @RequestParam("file") MultipartFile file) {
        service.save(id, file);
        return ResponseEntity.noContent().build();
    }
}
