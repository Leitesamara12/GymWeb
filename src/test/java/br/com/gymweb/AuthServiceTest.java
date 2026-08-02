package br.com.gymweb;

import br.com.gymweb.dto.LoginDTO;
import br.com.gymweb.dto.LoginResponseDTO;
import br.com.gymweb.model.Aluno;
import br.com.gymweb.model.Instrutor;
import br.com.gymweb.repository.AlunoRepository;
import br.com.gymweb.repository.InstrutorRepository;
import br.com.gymweb.service.AuthService;
import br.com.gymweb.service.exception.CredenciaisInvalidasException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private InstrutorRepository instrutorRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void configurar() {
        authService = new AuthService(instrutorRepository, alunoRepository, passwordEncoder);
    }

    @Test
    void deveAutenticarDono() {
        Instrutor instrutor = instrutor(1L, "Usuário mestre", "hash");
        when(instrutorRepository.findByCpf("12345678")).thenReturn(Optional.of(instrutor));
        when(instrutorRepository.findCargoByCpf("12345678")).thenReturn(Optional.of("DONO"));
        when(passwordEncoder.matches("admin", "hash")).thenReturn(true);

        LoginResponseDTO resposta = authService.autenticar(login("12345678", "admin"));

        assertEquals("DONO", resposta.getCargo());
        System.out.println("Cargo autenticado: " + resposta.getCargo());
    }

    @Test
    void deveAutenticarInstrutor() {
        Instrutor instrutor = instrutor(2L, "Instrutor", "hash");
        when(instrutorRepository.findByCpf("11111111111")).thenReturn(Optional.of(instrutor));
        when(instrutorRepository.findCargoByCpf("11111111111")).thenReturn(Optional.of("INSTRUTOR"));
        when(passwordEncoder.matches("senha", "hash")).thenReturn(true);

        LoginResponseDTO resposta = authService.autenticar(login("11111111111", "senha"));

        assertEquals("INSTRUTOR", resposta.getCargo());
    }

    @Test
    void deveAutenticarAluno() {
        Aluno aluno = aluno(3L, "Aluno", "hash");
        when(instrutorRepository.findByCpf("22222222222")).thenReturn(Optional.empty());
        when(alunoRepository.findByCpf("22222222222")).thenReturn(Optional.of(aluno));
        when(passwordEncoder.matches("senha", "hash")).thenReturn(true);

        LoginResponseDTO resposta = authService.autenticar(login("22222222222", "senha"));

        assertEquals("ALUNO", resposta.getCargo());
    }

    @Test
    void deveRecusarSenhaIncorreta() {
        Instrutor instrutor = mock(Instrutor.class);
        when(instrutor.getSenha()).thenReturn("hash");
        when(instrutorRepository.findByCpf("12345678")).thenReturn(Optional.of(instrutor));
        when(passwordEncoder.matches("incorreta", "hash")).thenReturn(false);

        assertThrows(
                CredenciaisInvalidasException.class,
                () -> authService.autenticar(login("12345678", "incorreta"))
        );
    }

    @Test
    void deveRecusarCpfInexistente() {
        when(instrutorRepository.findByCpf("00000000000")).thenReturn(Optional.empty());
        when(alunoRepository.findByCpf("00000000000")).thenReturn(Optional.empty());

        assertThrows(
                CredenciaisInvalidasException.class,
                () -> authService.autenticar(login("00000000000", "senha"))
        );
    }

    private LoginDTO login(String cpf, String senha) {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setCpf(cpf);
        loginDTO.setSenha(senha);
        return loginDTO;
    }

    private Instrutor instrutor(Long id, String nome, String hash) {
        Instrutor instrutor = mock(Instrutor.class);
        when(instrutor.getId()).thenReturn(id);
        when(instrutor.getNome()).thenReturn(nome);
        when(instrutor.getSenha()).thenReturn(hash);
        return instrutor;
    }

    private Aluno aluno(Long id, String nome, String hash) {
        Aluno aluno = mock(Aluno.class);
        when(aluno.getId()).thenReturn(id);
        when(aluno.getNome()).thenReturn(nome);
        when(aluno.getSenha()).thenReturn(hash);
        return aluno;
    }
}
