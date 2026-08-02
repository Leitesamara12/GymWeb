package br.com.gymweb.service;

import br.com.gymweb.model.Instrutor;
import br.com.gymweb.repository.InstrutorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null") // Resolve avisos de nulidade do VS Code/Java 21 em todo o arquivo
public class InstrutorService {

    private final InstrutorRepository repository;
    private final PasswordEncoder passwordEncoder;

    public InstrutorService(InstrutorRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Instrutor> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Instrutor buscarPorId(Long id) {
        if (id == null) throw new IllegalArgumentException("ID não pode ser nulo");
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Instrutor não encontrado com ID: " + id));
    }

    @Transactional(readOnly = true)
    public Optional<Instrutor> buscarPorCpf(String cpf) {
        return repository.findByCpf(cpf);
    }

    @Transactional
    public Instrutor criar(Instrutor instrutor) {
        if (instrutor == null) {
            throw new IllegalArgumentException("Instrutor inválido");
        }
        if (instrutor.getCpf() != null && repository.findByCpf(instrutor.getCpf()).isPresent()) {
            throw new IllegalArgumentException("CPF já cadastrado para outro instrutor");
        }
        if (instrutor.getSenha() == null || instrutor.getSenha().isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }

        instrutor.setSenha(passwordEncoder.encode(instrutor.getSenha()));
        instrutor.setCargo("INSTRUTOR");

        return repository.save(instrutor);
    }

    @Transactional
    public Instrutor atualizar(Long id, Instrutor atualizado) {
        if (id == null) throw new IllegalArgumentException("ID não pode ser nulo");
        Instrutor existente = buscarPorId(id);

        if (atualizado.getNome() != null && !atualizado.getNome().isBlank()) {
            existente.setNome(atualizado.getNome());
        }

        if (atualizado.getCpf() != null && !atualizado.getCpf().isBlank()) {
            Optional<Instrutor> porCpf = repository.findByCpf(atualizado.getCpf());
            if (porCpf.isPresent()) {
                long idEncontrado = porCpf.get().getId();
                long idAtual = id;
                if (idEncontrado != idAtual) {
                    throw new IllegalArgumentException("CPF já cadastrado para outro instrutor");
                }
            }
            existente.setCpf(atualizado.getCpf());
        }

        if (atualizado.getEmail() != null) existente.setEmail(atualizado.getEmail());
        if (atualizado.getTelefone() != null) existente.setTelefone(atualizado.getTelefone());
        if (atualizado.getEspecialidade() != null) existente.setEspecialidade(atualizado.getEspecialidade());

        if (atualizado.getSenha() != null && !atualizado.getSenha().isBlank()) {
            existente.setSenha(passwordEncoder.encode(atualizado.getSenha()));
        }

        return repository.save(existente);
    }

    @Transactional
    public void deletar(Long id) {
        if (id == null) throw new IllegalArgumentException("ID não pode ser nulo");
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Instrutor não encontrado com ID: " + id);
        }
        repository.deleteById(id);
    }
}
