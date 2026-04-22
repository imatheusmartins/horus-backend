package br.com.horus.horus_backend.dto.auth;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String email;
    private String senha;
}