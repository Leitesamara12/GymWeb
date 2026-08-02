package br.com.gymweb.service;

import br.com.gymweb.dto.LoginDTO;
import br.com.gymweb.dto.LoginResponseDTO;
import br.com.gymweb.model.Aluno;
import br.com.gymweb.model.Instrutor;
import br.com.gymweb.repository.AlunoRepository;
import br.com.gymweb.repository.InstrutorRepository;
import br.com.gymweb.service.exception.CredenciaisInvalidasException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * SERVIÇO DE AUTENTICAÇÃO — a "inteligência" do login do GymWeb.
 *
 * Fluxo de negócio implementado aqui:
 * 1. Recebe CPF + senha (via LoginDTO, vindo do LoginController do colega).
 * 2. Procura primeiro na tabela de INSTRUTORES (cobre DONO e INSTRUTOR).
 * 3. Se não achar, procura na tabela de ALUNOS.
 * 4. Se não achar em nenhuma das duas → credenciais inválidas.
 * 5. Se achar, valida a senha com hash BCrypt.
 * 6. Retorna um LoginResponseDTO com id, nome e cargo — é esse "cargo"
 * que o front-end (auth.js) usa para decidir a tela de destino.
 */
@Service
public class AuthService {

    private final InstrutorRepository instrutorRepository;
    private final AlunoRepository alunoRepository;
    private final PasswordEncoder passwordEncoder;

    /** Injeção via construtor — prática recomendada em vez de @Autowired em campo. */
    public AuthService(InstrutorRepository instrutorRepository,
                       AlunoRepository alunoRepository,
                       PasswordEncoder passwordEncoder) {
        this.instrutorRepository = instrutorRepository;
        this.alunoRepository = alunoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Método público que o LoginController do colega vai chamar.
     *
     * @param loginDTO cpf e senha digitados no front-end
     * @return dados de sessão (id, nome, cargo) se autenticação for válida
     * @throws CredenciaisInvalidasException se CPF não existir ou senha não bater
     */
    public LoginResponseDTO autenticar(LoginDTO loginDTO) {
        String cpf = loginDTO.getCpf();
        String senhaDigitada = loginDTO.getSenha();

        // 1ª tentativa: é um Instrutor ou Dono?
        Optional<Instrutor> instrutorOpt = instrutorRepository.findByCpf(cpf);
        if (instrutorOpt.isPresent()) {
            Instrutor instrutor = instrutorOpt.get();
            validarSenha(senhaDigitada, instrutor.getSenha());
            return new LoginResponseDTO(
                    instrutor.getId(),
                    instrutor.getNome(),
                    instrutorRepository.findCargoByCpf(cpf).orElse("INSTRUTOR")
            );
        }

        // 2ª tentativa: é um Aluno?
        Optional<Aluno> alunoOpt = alunoRepository.findByCpf(cpf);
        if (alunoOpt.isPresent()) {
            Aluno aluno = alunoOpt.get();
            validarSenha(senhaDigitada, aluno.getSenha());
            return new LoginResponseDTO(
                    aluno.getId(),
                    aluno.getNome(),
                    "ALUNO" // fixo, pois a tabela aluno não tem coluna "cargo"
            );
        }

        // CPF não encontrado em nenhuma tabela
        throw new CredenciaisInvalidasException("CPF ou senha inválidos.");
    }

    /**
     * Compara a senha digitada (texto plano) com o hash salvo no banco.
     * PasswordEncoder.matches() faz o hash da senha digitada internamente
     * e compara com o hash armazenado — nunca decodificamos o hash salvo.
     */
    private void validarSenha(String senhaDigitada, String hashArmazenado) {
        if (!passwordEncoder.matches(senhaDigitada, hashArmazenado)) {
            throw new CredenciaisInvalidasException("CPF ou senha inválidos.");
        }
    }
}
