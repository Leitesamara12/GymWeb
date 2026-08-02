package br.com.gymweb.repository;

import br.com.gymweb.model.Treino;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acesso aos dados dos treinos cadastrados. */
public interface TreinoRepository extends JpaRepository<Treino, Long> {

    List<Treino> findByAlunoId(Long alunoId);

    List<Treino> findByInstrutorId(Long instrutorId);
}
