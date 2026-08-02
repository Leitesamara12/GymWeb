package br.com.gymweb.repository;

import br.com.gymweb.model.ExercicioCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExercicioCatalogoRepository extends JpaRepository<ExercicioCatalogo, Long> {
    List<ExercicioCatalogo> findByGrupoMuscular(String grupoMuscular);
}
