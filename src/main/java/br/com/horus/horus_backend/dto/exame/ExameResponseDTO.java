package br.com.horus.horus_backend.dto.exame;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExameResponseDTO {
    private Long id;
    private Long pacienteId;
    private String nomePaciente;
    private String urlImagemOriginal;
    private String urlImagemAnotada;
    private LocalDateTime dataExame;
}