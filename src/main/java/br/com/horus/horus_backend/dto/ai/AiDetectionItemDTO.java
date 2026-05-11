package br.com.horus.horus_backend.dto.ai;

import lombok.Data;

@Data
public class AiDetectionItemDTO {
    private String label;
    private Double confidence;
    private AiBoundingBoxDTO bbox;
}
