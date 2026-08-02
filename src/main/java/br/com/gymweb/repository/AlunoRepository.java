package br.com.gymweb.repository;

import br.com.gymweb.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    Optional<Aluno> findByCpf(String cpf);

    List<Aluno> findByInstrutorIdOrderByNomeAsc(Long instrutorId);
}
