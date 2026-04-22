package br.com.horus.horus_backend.controller;

import br.com.horus.horus_backend.dto.exame.*;
import br.com.horus.horus_backend.service.ExameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/exames")
@RequiredArgsConstructor
public class ExameController {

    private final ExameService exameService;

    @PostMapping
    public ResponseEntity<ExameResponseDTO> criar(
            @RequestPart("dados") ExameRequestDTO dto,
            @RequestPart("imagem") MultipartFile imagem) {
        return ResponseEntity.ok(exameService.criar(dto, imagem));
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<ExameResponseDTO>> listarPorPaciente(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(exameService.listarPorPaciente(pacienteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExameResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(exameService.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        exameService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}