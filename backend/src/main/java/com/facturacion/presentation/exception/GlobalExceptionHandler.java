package com.facturacion.presentation.exception;

import com.facturacion.application.ports.output.InfraestructuraNoDisponibleException;
import com.facturacion.domain.exception.ConflictoDominioException;
import com.facturacion.domain.exception.EntidadNoEncontradaException;
import com.facturacion.domain.exception.ReglaNegocioException;
import com.facturacion.domain.exception.ValidacionDominioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntidadNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleNoEncontrada(EntidadNoEncontradaException exc) {
        return buildResponse(HttpStatus.NOT_FOUND, "No Encontrado", exc.getMessage(), Collections.emptyList());
    }

    @ExceptionHandler(ValidacionDominioException.class)
    public ResponseEntity<Map<String, Object>> handleValidacionDominio(ValidacionDominioException exc) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Validacion Invalida", exc.getMessage(), Collections.emptyList());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, Object>> handleReglaNegocio(ReglaNegocioException exc) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de Negocio Incumplida", exc.getMessage(), Collections.emptyList());
    }

    @ExceptionHandler(ConflictoDominioException.class)
    public ResponseEntity<Map<String, Object>> handleConflicto(ConflictoDominioException exc) {
        return buildResponse(HttpStatus.CONFLICT, "Conflicto", exc.getMessage(), Collections.emptyList());
    }

    @ExceptionHandler(InfraestructuraNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>> handleInfraNoDisponible(InfraestructuraNoDisponibleException exc) {
        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Base de Datos No Disponible (" + exc.getMotor() + ")",
                exc.getMessage(),
                Collections.emptyList()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleArgumentNotValid(MethodArgumentNotValidException exc) {
        List<String> errores = new ArrayList<>();
        for (FieldError fieldError : exc.getBindingResult().getFieldErrors()) {
            errores.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }
        return buildResponse(HttpStatus.BAD_REQUEST, "Errores de Validacion", "Parametros de entrada invalidos", errores);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception exc) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error Interno",
                exc.getMessage() == null ? "Ha ocurrido un error inesperado" : exc.getMessage(),
                Collections.emptyList()
        );
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String error,
            String message,
            List<String> detalles
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("detalles", detalles);
        return ResponseEntity.status(status).body(body);
    }
}
