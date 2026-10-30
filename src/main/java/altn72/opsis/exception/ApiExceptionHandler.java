package altn72.opsis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "altn72.opsis.controller.api")
public class ApiExceptionHandler {

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ApiError> handleRessourceIntrouvable(RessourceIntrouvableException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, e.getMessage(), Map.of(), LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(erreur -> erreurs.putIfAbsent(erreur.getField(), erreur.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ApiError(400, "Données invalides", erreurs, LocalDateTime.now()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleJsonIllisible(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(new ApiError(400, "Corps de requête invalide ou valeur inconnue", Map.of(), LocalDateTime.now()));
    }

    @ExceptionHandler(IdentifiantDejaUtiliseException.class)
    public ResponseEntity<ApiError> handleIdentifiantDejaUtilise(IdentifiantDejaUtiliseException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(409, e.getMessage(), Map.of(), LocalDateTime.now()));
    }

    @ExceptionHandler(IdentifiantsInvalidesException.class)
    public ResponseEntity<ApiError> handleIdentifiantsInvalides(IdentifiantsInvalidesException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError(401, e.getMessage(), Map.of(), LocalDateTime.now()));
    }
}