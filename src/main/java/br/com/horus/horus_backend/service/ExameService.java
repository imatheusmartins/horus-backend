package br.com.horus.horus_backend.service;

import br.com.horus.horus_backend.dto.ai.AiPredictionResponseDTO;
import br.com.horus.horus_backend.dto.exame.ExameRequestDTO;
import br.com.horus.horus_backend.dto.exame.ExameResponseDTO;
import br.com.horus.horus_backend.model.Exame;
import br.com.horus.horus_backend.model.Paciente;
import br.com.horus.horus_backend.repository.ExameRepository;
import br.com.horus.horus_backend.repository.PacienteRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExameService {

    private final ExameRepository exameRepository;
    private final PacienteRepository pacienteRepository;
    private final MinioStorageService minioStorageService;
    private final AiDetectionService aiDetectionService;
    private final ObjectMapper objectMapper;

    public ExameResponseDTO criar(ExameRequestDTO dto, MultipartFile imagem) {
        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente nao encontrado"));

        String urlOriginal = minioStorageService.upload(imagem, "original");
        AiPredictionResponseDTO analiseIA = aiDetectionService.analisar(imagem);

        Exame exame = new Exame();
        exame.setPaciente(paciente);
        exame.setUrlImagemOriginal(urlOriginal);
        exame.setUrlImagemAnotada(null);
        exame.setResultadoIa(toJson(analiseIA));

        exameRepository.save(exame);
        return toResponse(exame);
    }

    public List<ExameResponseDTO> listarPorPaciente(Long pacienteId) {
        return exameRepository.findByPacienteId(pacienteId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ExameResponseDTO buscarPorId(Long id) {
        Exame exame = exameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exame nao encontrado"));
        return toResponse(exame);
    }

    public void deletar(Long id) {
        exameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exame nao encontrado"));
        exameRepository.deleteById(id);
    }

    private ExameResponseDTO toResponse(Exame exame) {
        ExameResponseDTO response = new ExameResponseDTO();
        response.setId(exame.getId());
        response.setPacienteId(exame.getPaciente().getId());
        response.setNomePaciente(exame.getPaciente().getNome());
        response.setUrlImagemOriginal(exame.getUrlImagemOriginal());
        response.setUrlImagemAnotada(exame.getUrlImagemAnotada());
        response.setAnaliseIA(fromJson(exame.getResultadoIa()));
        response.setDataExame(exame.getDataExame());
        return response;
    }

    private String toJson(AiPredictionResponseDTO analiseIA) {
        try {
            return objectMapper.writeValueAsString(analiseIA);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar resposta da IA", e);
        }
    }

    private AiPredictionResponseDTO fromJson(String resultadoIa) {
        if (resultadoIa == null || resultadoIa.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(resultadoIa, AiPredictionResponseDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao desserializar resposta da IA", e);
        }
    }
}
