package br.com.horus.horus_backend.controller.handler;

import br.com.horus.horus_backend.dto.common.ApiErrorResponseDTO;
import br.com.horus.horus_backend.exception.AuthException;
import br.com.horus.horus_backend.exception.ConflictException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleAuthException(AuthException ex) {
        log.warn("Erro de autenticacao tratado: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), List.of());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleConflictException(ConflictException ex) {
        log.warn("Conflito tratado: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .toList();

        log.warn("Erro de validacao tratado: {}", details);
        return buildResponse(HttpStatus.BAD_REQUEST, "Dados invalidos na requisicao", details);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        log.warn("ResponseStatusException tratada. status={}, reason={}", status.value(), ex.getReason(), ex);
        return buildResponse(status, ex.getReason(), List.of());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
        log.warn("Parte obrigatoria ausente no multipart: {}", ex.getRequestPartName(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "Parte obrigatoria ausente: " + ex.getRequestPartName(), List.of());
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleMultipartException(MultipartException ex) {
        log.warn("Erro multipart tratado: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "Requisicao multipart invalida", List.of(ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleRuntimeException(RuntimeException ex) {
        log.error("RuntimeException nao esperada tratada pelo handler global", ex);
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
    }

    private ResponseEntity<ApiErrorResponseDTO> buildResponse(HttpStatus status, String message, List<String> details) {
        ApiErrorResponseDTO body = ApiErrorResponseDTO.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();

        return ResponseEntity.status(status).body(body);
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
