package br.com.horus.horus_backend.dto.auth;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String token;
}