package br.com.horus.horus_backend.service;

import br.com.horus.horus_backend.dto.ai.AiPredictionResponseDTO;
import br.com.horus.horus_backend.dto.ai.AiPredictionItemDTO;
import br.com.horus.horus_backend.dto.exame.ExameRequestDTO;
import br.com.horus.horus_backend.dto.exame.ExameResponseDTO;
import br.com.horus.horus_backend.model.Exame;
import br.com.horus.horus_backend.model.Paciente;
import br.com.horus.horus_backend.repository.ExameRepository;
import br.com.horus.horus_backend.repository.PacienteRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExameService {

    private final ExameRepository exameRepository;
    private final PacienteRepository pacienteRepository;
    private final AiDetectionService aiDetectionService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ExameResponseDTO criar(ExameRequestDTO dto, MultipartFile imagem) {
        if (dto == null || dto.getPacienteId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paciente do exame e obrigatorio");
        }

        log.info(
                "Criando exame. pacienteId={}, arquivo={}, contentType={}, tamanhoBytes={}",
                dto.getPacienteId(),
                imagem != null ? imagem.getOriginalFilename() : null,
                imagem != null ? imagem.getContentType() : null,
                imagem != null ? imagem.getSize() : null);

        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente nao encontrado"));

        Exame exame = exameRepository.saveAndFlush(novoExame(paciente));
        String urlImagemOriginal = null;
        String urlImagemAnotada = null;

        try {
            urlImagemOriginal = fileStorageService.salvarImagemExame(imagem, exame.getId(), "original");
            log.info("Imagem original salva. exameId={}, urlImagemOriginal={}", exame.getId(), urlImagemOriginal);

            AiPredictionResponseDTO analiseIA = aiDetectionService.analisar(imagem);
            urlImagemAnotada = getUrlImagemAnotada(analiseIA, exame.getId());

            exame.setUrlImagemOriginal(urlImagemOriginal);
            exame.setUrlImagemAnotada(urlImagemAnotada);
            exame.setResultadoIa(toJson(analiseIA));
            preencherPredicaoPrincipal(exame, analiseIA);

            exameRepository.save(exame);
            log.info(
                    "Exame salvo com analise de IA. exameId={}, pacienteId={}, topPredictionLabel={}, topPredictionConfidence={}",
                    exame.getId(),
                    paciente.getId(),
                    exame.getTopPredictionLabel(),
                    exame.getTopPredictionConfidence());
        } catch (RuntimeException e) {
            log.error(
                    "Erro ao criar exame. Limpando arquivos associados. exameId={}, urlImagemOriginal={}, urlImagemAnotada={}",
                    exame.getId(),
                    urlImagemOriginal,
                    urlImagemAnotada,
                    e);
            fileStorageService.deletarPorUrl(urlImagemOriginal);
            fileStorageService.deletarPorUrl(urlImagemAnotada);
            throw e;
        }

        return toResponse(exame);
    }

    @Transactional(readOnly = true)
    public List<ExameResponseDTO> listarPorPaciente(Long pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente nao encontrado");
        }

        return exameRepository.findByPacienteIdOrderByDataExameDesc(pacienteId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExameResponseDTO buscarPorId(Long id) {
        Exame exame = exameRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exame nao encontrado"));
        return toResponse(exame);
    }

    @Transactional
    public void deletar(Long id) {
        Exame exame = exameRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exame nao encontrado"));

        exameRepository.delete(exame);
        fileStorageService.deletarPorUrl(exame.getUrlImagemOriginal());
        fileStorageService.deletarPorUrl(exame.getUrlImagemAnotada());
    }

    private Exame novoExame(Paciente paciente) {
        Exame exame = new Exame();
        exame.setPaciente(paciente);
        return exame;
    }

    private void preencherPredicaoPrincipal(Exame exame, AiPredictionResponseDTO analiseIA) {
        if (analiseIA == null || analiseIA.getTopPrediction() == null) {
            return;
        }

        AiPredictionItemDTO topPrediction = analiseIA.getTopPrediction();
        exame.setTopPredictionLabel(topPrediction.getLabel());
        exame.setTopPredictionConfidence(topPrediction.getConfidence());
    }

    private String getUrlImagemAnotada(AiPredictionResponseDTO analiseIA, Long exameId) {
        if (analiseIA == null) {
            return null;
        }

        if (analiseIA.getAnnotatedImageUrl() != null && !analiseIA.getAnnotatedImageUrl().isBlank()) {
            return analiseIA.getAnnotatedImageUrl();
        }

        return fileStorageService.salvarBase64Exame(
                analiseIA.getAnnotatedImageBase64(),
                exameId,
                "anotada");
    }

    private ExameResponseDTO toResponse(Exame exame) {
        ExameResponseDTO response = new ExameResponseDTO();
        response.setId(exame.getId());
        response.setPacienteId(exame.getPaciente().getId());
        response.setNomePaciente(exame.getPaciente().getNome());
        response.setUrlImagemOriginal(exame.getUrlImagemOriginal());
        response.setUrlImagemAnotada(exame.getUrlImagemAnotada());
        response.setAnaliseIA(getAnaliseIa(exame));
        response.setDataExame(exame.getDataExame());
        return response;
    }

    private AiPredictionResponseDTO getAnaliseIa(Exame exame) {
        AiPredictionResponseDTO analiseIa = fromJson(exame.getResultadoIa(), exame.getId());
        if (analiseIa != null) {
            return analiseIa;
        }

        if (exame.getTopPredictionLabel() == null && exame.getTopPredictionConfidence() == null) {
            return null;
        }

        AiPredictionItemDTO topPrediction = new AiPredictionItemDTO();
        topPrediction.setLabel(exame.getTopPredictionLabel());
        topPrediction.setConfidence(exame.getTopPredictionConfidence());
        preencherDescricao(topPrediction);

        AiPredictionResponseDTO fallback = new AiPredictionResponseDTO();
        fallback.setTask("classify");
        fallback.setTopPrediction(topPrediction);
        fallback.setPredictions(List.of(topPrediction));
        fallback.setDetections(List.of());
        return fallback;
    }

    private String toJson(AiPredictionResponseDTO analiseIA) {
        try {
            return objectMapper.writeValueAsString(analiseIA);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar resposta da IA", e);
        }
    }

    private AiPredictionResponseDTO fromJson(String resultadoIa, Long exameId) {
        if (resultadoIa == null || resultadoIa.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(resultadoIa, AiPredictionResponseDTO.class);
        } catch (JsonProcessingException e) {
            log.warn(
                    "Resultado da IA invalido ou legado. Retornando fallback. exameId={}, resultadoIaPreview={}",
                    exameId,
                    preview(resultadoIa),
                    e);
            return null;
        }
    }

    private void preencherDescricao(AiPredictionItemDTO prediction) {
        if (prediction == null) {
            return;
        }

        String descricao = descreverGrau(prediction.getLabel());
        prediction.setDescription(descricao);
        prediction.setDescricao(descricao);
    }

    private String descreverGrau(String label) {
        if (label == null) {
            return "Grau não identificado";
        }

        return switch (label) {
            case "0" -> "Sem retinopatia diabética";
            case "1" -> "Retinopatia leve";
            case "2" -> "Retinopatia moderada";
            case "3" -> "Retinopatia grave";
            case "4" -> "Retinopatia diabética proliferativa";
            default -> "Grau não identificado";
        };
    }

    private String preview(String value) {
        if (value == null) {
            return null;
        }

        return value.length() <= 120
                ? value
                : value.substring(0, 120) + "...";
    }
}
