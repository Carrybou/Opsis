package altn72.opsis.exception.gestion;

import altn72.opsis.exception.acces.AccesRefuseException;
import altn72.opsis.exception.acces.IdentifiantsInvalidesException;
import altn72.opsis.exception.acces.ModificationIncidentInterditeException;
import altn72.opsis.exception.conflit.AffectationIncidentImpossibleException;
import altn72.opsis.exception.conflit.EvaluationImpossibleException;
import altn72.opsis.exception.conflit.IdentifiantDejaUtiliseException;
import altn72.opsis.exception.conflit.IncidentDejaResoluException;
import altn72.opsis.exception.introuvable.RessourceIntrouvableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "altn72.opsis.controller.api")
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ApiError> traiterIntrouvable(RessourceIntrouvableException e) {
        return reponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({ModificationIncidentInterditeException.class, AccesRefuseException.class})
    public ResponseEntity<ApiError> traiterInterdit(RuntimeException e) {
        return reponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler({AffectationIncidentImpossibleException.class, EvaluationImpossibleException.class,
            IncidentDejaResoluException.class, IdentifiantDejaUtiliseException.class})
    public ResponseEntity<ApiError> traiterConflit(RuntimeException e) {
        return reponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> traiterModificationSimultanee(OptimisticLockingFailureException e) {
        return reponse(HttpStatus.CONFLICT, "Ces données viennent d'être modifiées par un autre utilisateur : rechargez-les puis recommencez");
    }

    @ExceptionHandler(IdentifiantsInvalidesException.class)
    public ResponseEntity<ApiError> traiterIdentifiantsInvalides(IdentifiantsInvalidesException e) {
        return reponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> traiterValidation(MethodArgumentNotValidException e) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(erreur -> erreurs.putIfAbsent(erreur.getField(), erreur.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ApiError(HttpStatus.BAD_REQUEST.value(), "Données invalides", erreurs, LocalDateTime.now()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> traiterJsonIllisible(HttpMessageNotReadableException e) {
        return reponse(HttpStatus.BAD_REQUEST, "Corps de requête invalide ou valeur inconnue");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> traiterParametreInvalide(MethodArgumentTypeMismatchException e) {
        return reponse(HttpStatus.BAD_REQUEST, "Le paramètre « " + e.getName() + " » n'est pas valide");
    }

    // Les exceptions de Spring MVC portent leur propre code (415, 406…) : on le garde, mais jamais leur message, qui est technique.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> traiterInattendue(Exception e) {
        if (e instanceof ErrorResponse erreurHttp) {
            return reponse(HttpStatus.valueOf(erreurHttp.getStatusCode().value()), MessageErreurHttp.pour(erreurHttp.getStatusCode().value()));
        }
        log.error("Erreur inattendue sur l'API", e);
        return reponse(HttpStatus.INTERNAL_SERVER_ERROR, MessageErreurHttp.pour(HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    private static ResponseEntity<ApiError> reponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiError.de(status.value(), message));
    }
}
