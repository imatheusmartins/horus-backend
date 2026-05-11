package br.com.horus.horus_backend.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AiPredictionResponseDTO {
    private String task;

    @JsonProperty("model_source")
    private String modelSource;

    @JsonProperty("top_prediction")
    private AiPredictionItemDTO topPrediction;

    private List<AiPredictionItemDTO> predictions;
    private List<AiDetectionItemDTO> detections;
}
