package br.com.gymweb.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.gymweb.model.Aluno;
import br.com.gymweb.service.AlunoService;

/**
 * CRUD de alunos. Usado pelo INSTRUTOR (gerenciar seus alunos)
 * e parcialmente pelo próprio ALUNO (ver/editar seus dados).
 */
@RestController
@RequestMapping("/api/alunos")
@CrossOrigin(origins = "*")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @GetMapping
    public List<Aluno> listar() {
        return alunoService.listar();
    }

    @GetMapping("/instrutor/{instrutorId}")
    public List<Aluno> listarPorInstrutor(@PathVariable Long instrutorId) {
        return alunoService.listarPorInstrutor(instrutorId);
    }

    @GetMapping("/{id}")
    public Aluno buscarPorId(@PathVariable Long id) {
        return alunoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Aluno> criar(@RequestBody Aluno aluno,
                                        @RequestParam(required = false) Long instrutorId) {
        Aluno salvo = alunoService.criar(aluno, instrutorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping("/{id}")
    public Aluno atualizar(@PathVariable Long id, @RequestBody Aluno aluno) {
        return alunoService.atualizar(id, aluno);
    }

    @PutMapping("/{id}/instrutor/{instrutorId}")
    public ResponseEntity<Void> vincularInstrutor(@PathVariable Long id, @PathVariable Long instrutorId) {
        alunoService.vincularInstrutor(id, instrutorId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        alunoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
