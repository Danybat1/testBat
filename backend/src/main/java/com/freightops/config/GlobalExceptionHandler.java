package com.freightops.config;

import com.freightops.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire global d'exceptions pour FreightOps
 * Standardise les réponses d'erreur dans toute l'application
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Gestion des erreurs 404 - Ressource non trouvée
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleEntityNotFound(
            EntityNotFoundException ex, WebRequest request) {

        logger.warn("Entity not found: {} - Request: {}", ex.getMessage(), request.getDescription(false));

        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), "Ressource non trouvée");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Gestion des erreurs 404 - Endpoint non trouvé
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoHandlerFound(
            NoHandlerFoundException ex, WebRequest request) {

        logger.warn("No handler found for {} {} - Request: {}",
                ex.getHttpMethod(), ex.getRequestURL(), request.getDescription(false));

        String errorMessage = "Endpoint non trouvé: " + ex.getHttpMethod() + " " + ex.getRequestURL();
        ApiResponse<Object> response = ApiResponse.error(errorMessage, "Endpoint non trouvé");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Gestion des erreurs de validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {

        logger.warn("Validation error - Request: {}", request.getDescription(false));

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        ApiResponse<Map<String, String>> response = ApiResponse.success(errors, "Erreurs de validation");
        response.setSuccess(false);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Gestion des erreurs IllegalArgument
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {

        logger.warn("Illegal argument: {} - Request: {}", ex.getMessage(), request.getDescription(false));

        ApiResponse<Object> response = ApiResponse.error("Argument invalide: " + ex.getMessage(), "Argument invalide");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Gestion des erreurs génériques
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex, WebRequest request) {

        logger.error("Unexpected error - Request: {} - Error: {}",
                request.getDescription(false), ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.error("Erreur interne du serveur", "Erreur serveur");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Gestion des erreurs RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        logger.error("Runtime error - Request: {} - Error: {}",
                request.getDescription(false), ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.error("Erreur d'exécution: " + ex.getMessage(),
                "Erreur d'exécution");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
