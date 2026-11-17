package cl.duoc.seguros.siniestros;

import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiErrores {
    @ExceptionHandler({IllegalArgumentException.class, DateTimeParseException.class})
    ResponseEntity<Map<String, String>> datosInvalidos(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() == null ? "Datos inválidos" : e.getMessage()));
    }
    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<Map<String, String>> noExiste(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Siniestro o registro inexistente"));
    }
    @ExceptionHandler(SecurityException.class)
    ResponseEntity<Map<String, String>> acceso(Exception e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No tienes permiso para esta operación"));
    }
    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Map<String, String>> regla(Exception e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }
}
