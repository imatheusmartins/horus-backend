package br.com.horus.horus_backend.dto.paciente;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PacienteRequestDTO {
    private String nome;
    private LocalDate dataNascimento;
    private String cpf;
    private Long usuarioId;
}