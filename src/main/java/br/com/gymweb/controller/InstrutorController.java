package br.com.gymweb.controller;

import br.com.gymweb.model.Instrutor;
import br.com.gymweb.service.InstrutorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instrutores" )
public class InstrutorController {

    private final InstrutorService instrutorService;

    public InstrutorController(InstrutorService instrutorService) {
        this.instrutorService = instrutorService;
    }

    @PostMapping
    public ResponseEntity<Instrutor> criar(@RequestBody Instrutor instrutor, HttpSession session) {
        validarDono(session);
        // Garantindo que o instrutor não seja nulo para satisfazer o compilador
        if (instrutor == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.status(HttpStatus.CREATED).body(instrutorService.criar(instrutor));
    }

    @GetMapping
    public List<Instrutor> listar(HttpSession session) {
        validarDono(session);
        return instrutorService.listar();
    }

    @GetMapping("/{id}")
    public Instrutor buscarPorId(@PathVariable Long id, HttpSession session) {
        validarDono(session);
        if (id == null) throw new IllegalArgumentException("ID não pode ser nulo");
        return instrutorService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Instrutor atualizar(@PathVariable Long id, @RequestBody Instrutor instrutor, HttpSession session) {
        validarDono(session);
        if (id == null || instrutor == null) throw new IllegalArgumentException("ID e dados são obrigatórios");
        return instrutorService.atualizar(id, instrutor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, HttpSession session) {
        validarDono(session);
        if (id == null) throw new IllegalArgumentException("ID não pode ser nulo");
        instrutorService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private void validarDono(HttpSession session) {
        Object cargoObj = session.getAttribute("cargo");
        String cargo = cargoObj != null ? cargoObj.toString() : "";
        if (!"DONO".equalsIgnoreCase(cargo)) {
            throw new AcessoNegadoException("Apenas o administrador (DONO) pode gerenciar instrutores.");
        }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class AcessoNegadoException extends RuntimeException {
        public AcessoNegadoException(String message) {
            super(message);
        }
    }
}
