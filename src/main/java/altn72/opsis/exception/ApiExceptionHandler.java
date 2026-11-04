package altn72.opsis.exception;

import org.springframework.dao.OptimisticLockingFailureException;
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

    @ExceptionHandler(ModificationIncidentInterditeException.class)
    public ResponseEntity<ApiError> handleModificationInterdite(ModificationIncidentInterditeException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiError(403, e.getMessage(), Map.of(), LocalDateTime.now()));
    }

    @ExceptionHandler(AffectationIncidentImpossibleException.class)
    public ResponseEntity<ApiError> handleAffectationImpossible(AffectationIncidentImpossibleException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(409, e.getMessage(), Map.of(), LocalDateTime.now()));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleModificationSimultanee(OptimisticLockingFailureException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(409, "Ces données viennent d'être modifiées par un autre utilisateur : rechargez-les puis recommencez",
                        Map.of(), LocalDateTime.now()));
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

    @ExceptionHandler(AccesRefuseException.class)
    public ResponseEntity<ApiError> handleAccesRefuse(AccesRefuseException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiError(403, e.getMessage(), Map.of(), LocalDateTime.now()));
    }

    @ExceptionHandler(EvaluationImpossibleException.class)
    public ResponseEntity<ApiError> handleEvaluationImpossible(EvaluationImpossibleException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(409, e.getMessage(), Map.of(), LocalDateTime.now()));
    }

    @ExceptionHandler(IncidentDejaResoluException.class)
    public ResponseEntity<ApiError> handleDejaResolu(IncidentDejaResoluException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(409, e.getMessage(), Map.of(), LocalDateTime.now()));
    }
}