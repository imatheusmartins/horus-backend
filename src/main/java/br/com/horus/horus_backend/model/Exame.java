package br.com.horus.horus_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "exames")
public class Exame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    private String urlImagemOriginal;

    private String urlImagemAnotada;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String resultadoIa;

    private LocalDateTime dataExame;

    @PrePersist
    public void prePersist() {
        this.dataExame = LocalDateTime.now();
    }
}
