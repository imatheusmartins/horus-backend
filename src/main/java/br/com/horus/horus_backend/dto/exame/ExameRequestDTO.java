package br.com.horus.horus_backend.dto.exame;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExameRequestDTO {
    @NotNull(message = "Paciente e obrigatorio")
    private Long pacienteId;
}
