package br.com.gymweb.service;

import br.com.gymweb.model.ExercicioCatalogo;
import br.com.gymweb.repository.ExercicioCatalogoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@SuppressWarnings("null") // Resolve avisos de nulidade do VS Code/Java 21 em todo o arquivo
public class ExercicioCatalogoService {

    private final ExercicioCatalogoRepository repository;

    public ExercicioCatalogoService(ExercicioCatalogoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ExercicioCatalogo> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ExercicioCatalogo> listarPorGrupoMuscular(String grupoMuscular) {
        if (grupoMuscular == null || grupoMuscular.isBlank()) return repository.findAll();
        return repository.findByGrupoMuscular(grupoMuscular);
    }

    @Transactional(readOnly = true)
    public ExercicioCatalogo buscarPorId(Long id) {
        if (id == null) throw new IllegalArgumentException("ID não pode ser nulo");
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Exercício não encontrado com ID: " + id));
    }

    @Transactional
    public ExercicioCatalogo criar(ExercicioCatalogo exercicio) {
        if (exercicio == null || exercicio.getNome() == null || exercicio.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do exercício é obrigatório");
        }
        return repository.save(exercicio);
    }

    @Transactional
    public ExercicioCatalogo atualizar(Long id, ExercicioCatalogo atualizado) {
        if (id == null) throw new IllegalArgumentException("ID não pode ser nulo");
        ExercicioCatalogo existente = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Exercício não encontrado com ID: " + id));
        
        if (atualizado.getNome() != null && !atualizado.getNome().isBlank()) existente.setNome(atualizado.getNome());
        if (atualizado.getDescricao() != null) existente.setDescricao(atualizado.getDescricao());
        if (atualizado.getGrupoMuscular() != null) existente.setGrupoMuscular(atualizado.getGrupoMuscular());
        
        return repository.save(existente);
    }

    @Transactional
    public void deletar(Long id) {
        if (id == null) throw new IllegalArgumentException("ID não pode ser nulo");
        if (!repository.existsById(id)) throw new IllegalArgumentException("Exercício não encontrado com ID: " + id);
        repository.deleteById(id);
    }
}
