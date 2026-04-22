package br.com.horus.horus_backend.dto.auth;

import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String nome;
    private String email;
    private String senha;
}