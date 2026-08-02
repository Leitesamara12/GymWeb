package br.com.gymweb.controller;

import br.com.gymweb.dto.LoginResponseDTO;
import br.com.gymweb.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginControllerTest {

    @Test
    void deveRegistrarSessaoAoLogarComCredenciaisValidas( ) {
        AuthService authService = mock(AuthService.class);
        when(authService.autenticar(any())).thenReturn(new LoginResponseDTO(1L, "Administrador", "DONO"));
        LoginController controller = new LoginController(authService);
        MockHttpSession session = new MockHttpSession();

        LoginController.LoginRequest request = new LoginController.LoginRequest();
        request.setCpf("00000000000");
        request.setSenha("admin123");

        ResponseEntity<Map<String, Object>> responseEntity = controller.login(request, session);
        Map<String, Object> response = responseEntity.getBody();

        // Garante que a resposta não é nula antes de acessar os dados
        assertNotNull(response, "O corpo da resposta não deve ser nulo");
        
        assertEquals(true, response.get("success"));
        assertEquals(1L, session.getAttribute("usuarioId"));
        assertEquals("DONO", session.getAttribute("cargo"));
    }
}
