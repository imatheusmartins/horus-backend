package br.com.horus.horus_backend.dto.exame;

import br.com.horus.horus_backend.dto.ai.AiPredictionResponseDTO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExameResponseDTO {
    private Long id;
    private Long pacienteId;
    private String nomePaciente;
    private String urlImagemOriginal;
    private String urlImagemAnotada;
    private AiPredictionResponseDTO analiseIA;
    private LocalDateTime dataExame;
}
