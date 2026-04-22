package br.com.horus.horus_backend.repository;

import br.com.horus.horus_backend.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    List<Paciente> findByUsuarioId(Long usuarioId);
}