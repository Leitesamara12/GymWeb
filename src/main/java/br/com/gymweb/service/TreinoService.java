package br.com.gymweb.service;

import br.com.gymweb.dto.ExercicioResumoDTO;
import br.com.gymweb.dto.ExercicioTreinoDTO;
import br.com.gymweb.dto.ExercicioTreinoResponseDTO;
import br.com.gymweb.dto.TreinoDTO;
import br.com.gymweb.dto.TreinoResponseDTO;
import br.com.gymweb.dto.UsuarioResumoDTO;
import br.com.gymweb.model.ExercicioCatalogo;
import br.com.gymweb.model.ExercicioTreino;
import br.com.gymweb.model.Treino;
import br.com.gymweb.repository.AlunoRepository;
import br.com.gymweb.repository.ExercicioCatalogoRepository;
import br.com.gymweb.repository.ExercicioTreinoRepository;
import br.com.gymweb.repository.InstrutorRepository;
import br.com.gymweb.repository.TreinoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@SuppressWarnings("null") // Resolve avisos de nulidade do VS Code/Java 21 em todo o arquivo
public class TreinoService {

    private final TreinoRepository treinoRepository;
    private final ExercicioTreinoRepository exercicioTreinoRepository;
    private final ExercicioCatalogoRepository exercicioCatalogoRepository;
    private final AlunoRepository alunoRepository;
    private final InstrutorRepository instrutorRepository;

    public TreinoService(
            TreinoRepository treinoRepository,
            ExercicioTreinoRepository exercicioTreinoRepository,
            ExercicioCatalogoRepository exercicioCatalogoRepository,
            AlunoRepository alunoRepository,
            InstrutorRepository instrutorRepository
    ) {
        this.treinoRepository = treinoRepository;
        this.exercicioTreinoRepository = exercicioTreinoRepository;
        this.exercicioCatalogoRepository = exercicioCatalogoRepository;
        this.alunoRepository = alunoRepository;
        this.instrutorRepository = instrutorRepository;
    }

    @Transactional(readOnly = true)
    public List<TreinoResponseDTO> listarTodosTreinos() {
        return treinoRepository.findAll().stream().map(this::paraResposta).toList();
    }

    @Transactional(readOnly = true)
    public TreinoResponseDTO buscarPorId(Long id) {
        if (id == null) throw new IllegalArgumentException("ID do treino não pode ser nulo");
        return paraResposta(buscarEntidadePorId(id));
    }

    @Transactional(readOnly = true)
    public List<TreinoResponseDTO> buscarTreinosPorAluno(Long alunoId) {
        if (alunoId == null) throw new IllegalArgumentException("ID do aluno não pode ser nulo");
        return treinoRepository.findByAlunoId(alunoId).stream().map(this::paraResposta).toList();
    }

    @Transactional(readOnly = true)
    public List<TreinoResponseDTO> buscarTreinosPorInstrutor(Long instrutorId) {
        if (instrutorId == null) throw new IllegalArgumentException("ID do instrutor não pode ser nulo");
        return treinoRepository.findByInstrutorId(instrutorId).stream().map(this::paraResposta).toList();
    }

    @Transactional(readOnly = true)
    public List<ExercicioTreinoResponseDTO> listarExerciciosDoTreino(Long treinoId) {
        if (treinoId == null) throw new IllegalArgumentException("ID do treino não pode ser nulo");
        return exercicioTreinoRepository.findByTreinoIdOrderByOrdemAsc(treinoId)
                .stream()
                .map(this::paraResposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TreinoResponseDTO> listarPorAluno(Long alunoId) {
        return buscarTreinosPorAluno(alunoId);
    }

    @Transactional(readOnly = true)
    public List<TreinoResponseDTO> listarPorInstrutor(Long instrutorId) {
        return buscarTreinosPorInstrutor(instrutorId);
    }

    @Transactional
    public TreinoResponseDTO criar(TreinoDTO treinoDTO) {
        Treino treino = mapDtoToTreino(treinoDTO);
        validarTreino(treino);
        return paraResposta(treinoRepository.save(treino));
    }

    @Transactional
    public TreinoResponseDTO atualizar(Long id, TreinoDTO treinoDTO) {
        if (id == null) throw new IllegalArgumentException("ID do treino não pode ser nulo");
        Treino atualizado = mapDtoToTreino(treinoDTO);
        validarTreino(atualizado);

        Treino existente = buscarEntidadePorId(id);
        existente.setNome(atualizado.getNome());
        existente.setDescricao(atualizado.getDescricao());
        existente.setAluno(atualizado.getAluno());
        existente.setInstrutor(atualizado.getInstrutor());
        existente.getExercicios().clear();

        for (ExercicioTreino exercicio : atualizado.getExercicios()) {
            existente.addExercicio(
                    exercicio.getExercicio(),
                    exercicio.getOrdem(),
                    exercicio.getSeries(),
                    exercicio.getRepeticoes(),
                    exercicio.getCarga()
            );
        }
        return paraResposta(treinoRepository.save(existente));
    }

    @Transactional
    public void deletar(Long id) {
        if (id == null) throw new IllegalArgumentException("ID do treino não pode ser nulo");
        if (!treinoRepository.existsById(id)) {
            throw new IllegalArgumentException("Treino não encontrado com ID: " + id);
        }
        treinoRepository.deleteById(id);
    }

    @Transactional
    public ExercicioTreinoResponseDTO marcarConcluido(Long treinoId, Long exercicioTreinoId, boolean concluido) {
        if (treinoId == null || exercicioTreinoId == null) throw new IllegalArgumentException("IDs não podem ser nulos");
        
        ExercicioTreino exercicio = exercicioTreinoRepository.findById(exercicioTreinoId)
                .orElseThrow(() -> new IllegalArgumentException("Exercício do treino não encontrado com ID: " + exercicioTreinoId));
        
        if (exercicio.getTreino() == null || !treinoId.equals(exercicio.getTreino().getId())) {
            throw new IllegalArgumentException("O exercício não pertence ao treino informado.");
        }
        
        exercicio.setConcluido(concluido);
        return paraResposta(exercicioTreinoRepository.save(exercicio));
    }

    private Treino buscarEntidadePorId(Long id) {
        return treinoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Treino não encontrado com ID: " + id));
    }

    private Treino mapDtoToTreino(TreinoDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Dados do treino são obrigatórios.");
        }

        Treino treino = new Treino();
        treino.setNome(dto.getNome());
        treino.setDescricao(dto.getDescricao());

        if (dto.getAlunoId() == null) {
            throw new IllegalArgumentException("Aluno é obrigatório.");
        }
        treino.setAluno(alunoRepository.findById(dto.getAlunoId())
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado com ID: " + dto.getAlunoId())));

        if (dto.getInstrutorId() == null) {
            throw new IllegalArgumentException("Instrutor é obrigatório.");
        }
        treino.setInstrutor(instrutorRepository.findById(dto.getInstrutorId())
                .orElseThrow(() -> new IllegalArgumentException("Instrutor não encontrado com ID: " + dto.getInstrutorId())));

        List<ExercicioTreinoDTO> exercicios = dto.getExercicios() == null ? List.of() : dto.getExercicios();
        int proximaOrdem = 1;
        for (ExercicioTreinoDTO item : exercicios) {
            if (item == null || item.getExercicioCatalogoId() == null) {
                throw new IllegalArgumentException("Cada exercício precisa informar o identificador do catálogo.");
            }

            ExercicioCatalogo catalogo = exercicioCatalogoRepository.findById(item.getExercicioCatalogoId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Exercício do catálogo não encontrado com ID: " + item.getExercicioCatalogoId()));

            int ordem = item.getOrdem() == null ? proximaOrdem : item.getOrdem();
            int series = item.getSeries() == null ? 3 : item.getSeries();
            int repeticoes = item.getRepeticoes() == null ? 10 : item.getRepeticoes();
            double carga = item.getCarga() == null ? 0.0 : item.getCarga();
            treino.addExercicio(catalogo, ordem, series, repeticoes, carga);
            proximaOrdem++;
        }

        return treino;
    }

    private void validarTreino(Treino treino) {
        if (treino == null || treino.getNome() == null || treino.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do treino é obrigatório.");
        }
        if (treino.getAluno() == null) {
            throw new IllegalArgumentException("Aluno é obrigatório.");
        }
        if (treino.getInstrutor() == null) {
            throw new IllegalArgumentException("Instrutor é obrigatório.");
        }
        if (treino.getExercicios() == null || treino.getExercicios().isEmpty()) {
            throw new IllegalArgumentException("Informe pelo menos um exercício para o treino.");
        }
    }

    private TreinoResponseDTO paraResposta(Treino treino) {
        List<ExercicioTreinoResponseDTO> exercicios = treino.getExercicios().stream()
                .sorted(Comparator.comparing(ExercicioTreino::getOrdem, Comparator.nullsLast(Integer::compareTo)))
                .map(this::paraResposta)
                .toList();

        return new TreinoResponseDTO(
                treino.getId(),
                treino.getNome(),
                treino.getDescricao(),
                new UsuarioResumoDTO(treino.getAluno().getId(), treino.getAluno().getNome()),
                new UsuarioResumoDTO(treino.getInstrutor().getId(), treino.getInstrutor().getNome()),
                exercicios
        );
    }

    private ExercicioTreinoResponseDTO paraResposta(ExercicioTreino exercicioTreino) {
        ExercicioCatalogo exercicio = exercicioTreino.getExercicio();
        return new ExercicioTreinoResponseDTO(
                exercicioTreino.getId(),
                new ExercicioResumoDTO(
                        exercicio.getId(),
                        exercicio.getNome(),
                        exercicio.getDescricao(),
                        exercicio.getGrupoMuscular()
                ),
                exercicioTreino.getOrdem(),
                exercicioTreino.getSeries(),
                exercicioTreino.getRepeticoes(),
                exercicioTreino.getCarga(),
                exercicioTreino.getConcluido()
        );
    }
}
