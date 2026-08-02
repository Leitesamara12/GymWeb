package br.com.gymweb.service;

import br.com.gymweb.model.Aluno;
import br.com.gymweb.model.Instrutor;
import br.com.gymweb.repository.AlunoRepository;
import br.com.gymweb.repository.InstrutorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null") // Resolve avisos de nulidade em todo o arquivo de forma limpa
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final InstrutorRepository instrutorRepository;
    private final PasswordEncoder passwordEncoder;

    public AlunoService(
            AlunoRepository alunoRepository,
            InstrutorRepository instrutorRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.alunoRepository = alunoRepository;
        this.instrutorRepository = instrutorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Aluno> listar() {
        return alunoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Aluno> listarPorInstrutor(Long instrutorId) {
        return alunoRepository.findByInstrutorIdOrderByNomeAsc(instrutorId);
    }

    @Transactional(readOnly = true)
    public Aluno buscarPorId(Long id) {
        if (id == null) throw new IllegalArgumentException("ID não pode ser nulo");
        return alunoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado com ID: " + id));
    }

    @Transactional
    public Aluno criar(Aluno aluno, Long instrutorId) {
        if (aluno == null) {
            throw new IllegalArgumentException("Aluno inválido");
        }
        if (aluno.getCpf() != null && alunoRepository.findByCpf(aluno.getCpf()).isPresent()) {
            throw new IllegalArgumentException("CPF já cadastrado para outro aluno");
        }
        if (aluno.getSenha() == null || aluno.getSenha().isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }

        aluno.setSenha(passwordEncoder.encode(aluno.getSenha()));
        aluno.setCargo("ALUNO");

        if (instrutorId != null) {
            Instrutor instrutor = instrutorRepository.findById(instrutorId)
                    .orElseThrow(() -> new IllegalArgumentException("Instrutor não encontrado com ID: " + instrutorId));
            aluno.setInstrutor(instrutor);
        }

        return alunoRepository.save(aluno);
    }

    @Transactional
    public Aluno atualizar(Long id, Aluno alunoAtualizado) {
        if (id == null) throw new IllegalArgumentException("ID não pode ser nulo");
        if (alunoAtualizado == null) throw new IllegalArgumentException("Dados de atualização nulos");
        
        Aluno existente = buscarPorId(id);

        if (alunoAtualizado.getNome() != null && !alunoAtualizado.getNome().isBlank()) {
            existente.setNome(alunoAtualizado.getNome());
        }

        if (alunoAtualizado.getCpf() != null && !alunoAtualizado.getCpf().isBlank()) {
            Optional<Aluno> porCpf = alunoRepository.findByCpf(alunoAtualizado.getCpf());
            if (porCpf.isPresent()) {
                long idEncontrado = porCpf.get().getId();
                long idAtual = id;
                if (idEncontrado != idAtual) {
                    throw new IllegalArgumentException("CPF já cadastrado para outro aluno");
                }
            }
            existente.setCpf(alunoAtualizado.getCpf());
        }

        if (alunoAtualizado.getSenha() != null && !alunoAtualizado.getSenha().isBlank()) {
            existente.setSenha(passwordEncoder.encode(alunoAtualizado.getSenha()));
        }

        return alunoRepository.save(existente);
    }

    @Transactional
    public void vincularInstrutor(Long alunoId, Long instrutorId) {
        if (alunoId == null || instrutorId == null) throw new IllegalArgumentException("IDs não podem ser nulos");
        Aluno aluno = buscarPorId(alunoId);
        Instrutor instrutor = instrutorRepository.findById(instrutorId)
                .orElseThrow(() -> new IllegalArgumentException("Instrutor não encontrado com ID: " + instrutorId));
        aluno.setInstrutor(instrutor);
        
        alunoRepository.save(aluno);
    }

    @Transactional
    public void deletar(Long id) {
        if (id == null) throw new IllegalArgumentException("ID não pode ser nulo");
        if (!alunoRepository.existsById(id)) {
            throw new IllegalArgumentException("Aluno não encontrado com ID: " + id);
        }
        alunoRepository.deleteById(id);
    }
}
