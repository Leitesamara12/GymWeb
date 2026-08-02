package br.com.gymweb.controller;

import br.com.gymweb.dto.LoginDTO;
import br.com.gymweb.dto.LoginResponseDTO;
import br.com.gymweb.service.AuthService;
import br.com.gymweb.service.exception.CredenciaisInvalidasException;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    public static class LoginRequest {
        private String username;
        private String password;
        private String cpf;
        private String senha;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getCpf() {
            return cpf;
        }

        public void setCpf(String cpf) {
            this.cpf = cpf;
        }

        public String getSenha() {
            return senha;
        }

        public void setSenha(String senha) {
            this.senha = senha;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request, HttpSession session) {
        String cpf = firstNonBlank(request.getCpf(), request.getUsername());
        String senha = firstNonBlank(request.getSenha(), request.getPassword());

        if (isBlank(cpf) || isBlank(senha)) {
            return ResponseEntity.badRequest().body(errorResponse("CPF e senha são obrigatórios."));
        }

        try {
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setCpf(cpf.replaceAll("\\D", ""));
            loginDTO.setSenha(senha);
            LoginResponseDTO responseDTO = authService.autenticar(loginDTO);

            session.setAttribute("usuarioId", responseDTO.getId());
            session.setAttribute("cargo", responseDTO.getCargo().toUpperCase());
            session.setAttribute("usuario", responseDTO.getNome());

            return ResponseEntity.ok(successResponse(responseDTO));
        } catch (CredenciaisInvalidasException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse("CPF ou senha inválidos."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessao")
    public ResponseEntity<Map<String, Object>> sessao(HttpSession session) {
        Object id = session.getAttribute("usuarioId");
        Object cargo = session.getAttribute("cargo");
        Object nome = session.getAttribute("usuario");
        if (id == null || cargo == null || nome == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse("Sessão não encontrada."));
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("success", true);
        payload.put("id", id);
        payload.put("nome", nome);
        payload.put("cargo", cargo);
        return ResponseEntity.ok(payload);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Map<String, Object> successResponse(LoginResponseDTO responseDTO) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("success", true);
        payload.put("message", "Login realizado com sucesso.");
        payload.put("id", responseDTO.getId());
        payload.put("nome", responseDTO.getNome());
        payload.put("cargo", responseDTO.getCargo());
        return payload;
    }

    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("success", false);
        payload.put("message", message);
        return payload;
    }
}
