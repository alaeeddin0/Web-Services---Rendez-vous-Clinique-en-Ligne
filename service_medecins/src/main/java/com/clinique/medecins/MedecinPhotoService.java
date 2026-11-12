package com.clinique.medecins;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class MedecinPhotoService {
    private static final long MAX_BYTES = 2 * 1024 * 1024;
    private static final int MAX_DIMENSION = 4096;
    private final MedecinRepository doctors;
    private final MedecinPhotoRepository photos;

    public MedecinPhotoService(MedecinRepository doctors, MedecinPhotoRepository photos) {
        this.doctors = doctors;
        this.photos = photos;
    }

    @Transactional(readOnly = true)
    public Photo get(Long id) {
        var photo = photos.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucune photo pour ce médecin."));
        return new Photo(photo.contentType, photo.imageData);
    }

    public void save(Long id, MultipartFile file) {
        var doctor = doctors.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Médecin introuvable."));
        if (file.isEmpty()) throw invalid("Choisissez une image JPEG ou PNG.");
        if (file.getSize() > MAX_BYTES)
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "La photo ne doit pas dépasser 2 Mo.");
        Photo sanitized = readImage(file);
        var photo = photos.findById(id).orElseGet(MedecinPhoto::new);
        photo.medecin = doctor;
        photo.contentType = sanitized.contentType();
        photo.imageData = sanitized.bytes();
        photos.saveAndFlush(photo);
    }

    private Photo readImage(MultipartFile file) {
        try (var uploaded = file.getInputStream();
             var input = ImageIO.createImageInputStream(uploaded)) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw invalid("Le fichier doit être une image JPEG ou PNG valide.");
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!format.equals("jpeg") && !format.equals("png"))
                    throw invalid("Seuls les formats JPEG et PNG sont acceptés.");
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width < 1 || height < 1 || width > MAX_DIMENSION || height > MAX_DIMENSION)
                    throw invalid("La photo doit mesurer au maximum 4096 × 4096 pixels.");
                BufferedImage image = reader.read(0);
                // Re-encoding strips uploaded metadata and any appended non-image content.
                var output = new ByteArrayOutputStream();
                if (!ImageIO.write(image, format, output)) throw invalid("Image illisible.");
                if (output.size() > MAX_BYTES)
                    throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "La photo décodée dépasse 2 Mo. Réduisez sa résolution.");
                return new Photo(format.equals("png") ? "image/png" : "image/jpeg", output.toByteArray());
            } finally { reader.dispose(); }
        } catch (IOException | IllegalArgumentException ex) {
            throw invalid("Le fichier doit être une image JPEG ou PNG valide.");
        }
    }

    private static ResponseStatusException invalid(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
    public record Photo(String contentType, byte[] bytes) {}
}
