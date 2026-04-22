package br.com.horus.horus_backend.dto.usuario;

import lombok.Data;

@Data
public class UsuarioRequestDTO {
    private String nome;
    private String email;
    private String senha;
}