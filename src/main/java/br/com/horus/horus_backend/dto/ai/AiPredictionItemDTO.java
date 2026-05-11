package br.com.horus.horus_backend.dto.ai;

import lombok.Data;

@Data
public class AiPredictionItemDTO {
    private String label;
    private Double confidence;
}
