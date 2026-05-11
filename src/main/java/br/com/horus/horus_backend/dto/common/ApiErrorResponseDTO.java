package br.com.horus.horus_backend.dto.common;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ApiErrorResponseDTO(
        int status,
        String error,
        String message,
        LocalDateTime timestamp,
        List<String> details
) {
}
