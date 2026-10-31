package com.example.service_creneaux.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ErreurResponse> gererRessourceIntrouvable(
            RessourceIntrouvableException exception) {
        return erreur(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErreurResponse> gererMauvaiseRequete(
            IllegalArgumentException exception) {
        return erreur(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErreurResponse> gererConflit(
            IllegalStateException exception) {
        return erreur(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurResponse> gererValidation(
            MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(erreur -> erreur.getDefaultMessage())
                .orElse("La requête est invalide");

        return erreur(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseEntity<ErreurResponse> erreur(
            HttpStatus status,
            String message) {
        ErreurResponse response = new ErreurResponse(
                java.time.Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message);

        return ResponseEntity.status(status).body(response);
    }
}