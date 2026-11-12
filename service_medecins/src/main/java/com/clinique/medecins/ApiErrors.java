package com.clinique.medecins;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestControllerAdvice
public class ApiErrors {
 @ExceptionHandler(MethodArgumentNotValidException.class)
 ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
  Map<String,String> fields=new LinkedHashMap<>();
  ex.getBindingResult().getFieldErrors().forEach(e -> fields.putIfAbsent(e.getField(),e.getDefaultMessage()));
  return ResponseEntity.badRequest().body(Map.of("message","Vérifiez les champs du formulaire.","errors",fields));
 }
 @ExceptionHandler(MaxUploadSizeExceededException.class)
 ResponseEntity<?> tooLarge() {
  return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of("message","La photo ne doit pas dépasser 2 Mo."));
 }
 @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
  HandlerMethodValidationException.class, MissingServletRequestPartException.class, MissingServletRequestParameterException.class})
 ResponseEntity<?> badRequest(Exception ex) {
  String message = ex instanceof HandlerMethodValidationException || ex instanceof MethodArgumentTypeMismatchException
   ? "L'identifiant du médecin doit être un entier positif." : "Le contenu de la requête est invalide ou incomplet.";
  return ResponseEntity.badRequest().body(Map.of("message",message));
 }
 @ExceptionHandler(ResponseStatusException.class)
 ResponseEntity<?> status(ResponseStatusException ex) {
  return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message",ex.getReason()==null ? "Requête invalide." : ex.getReason()));
 }
 @ExceptionHandler(DataIntegrityViolationException.class)
 ResponseEntity<?> conflict() {
  return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","Conflit avec les données existantes."));
 }
}
