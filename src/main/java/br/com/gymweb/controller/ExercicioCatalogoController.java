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

import br.com.gymweb.model.ExercicioCatalogo;
import br.com.gymweb.service.ExercicioCatalogoService;
import jakarta.servlet.http.HttpSession;

/**
 * CRUD do catálogo de exercícios (mantido pelo INSTRUTOR/DONO,
 * usado para montar os treinos). Leitura fica liberada a qualquer
 * usuário logado; escrita é restrita à equipe.
 */
@RestController
@RequestMapping("/api/exercicios")
@CrossOrigin(origins = "*")
public class ExercicioCatalogoController {

    private final ExercicioCatalogoService exercicioCatalogoService;

    public ExercicioCatalogoController(ExercicioCatalogoService exercicioCatalogoService) {
        this.exercicioCatalogoService = exercicioCatalogoService;
    }

    @GetMapping
    public List<ExercicioCatalogo> listar(@RequestParam(required = false) String grupoMuscular) {
        if (grupoMuscular != null && !grupoMuscular.isBlank()) {
            return exercicioCatalogoService.listarPorGrupoMuscular(grupoMuscular);
        }
        return exercicioCatalogoService.listar();
    }

    @GetMapping("/{id}")
    public ExercicioCatalogo buscarPorId(@PathVariable Long id) {
        return exercicioCatalogoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ExercicioCatalogo> criar(@RequestBody ExercicioCatalogo exercicio, HttpSession session) {
        validarEquipe(session);
        ExercicioCatalogo salvo = exercicioCatalogoService.criar(exercicio);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping("/{id}")
    public ExercicioCatalogo atualizar(@PathVariable Long id, @RequestBody ExercicioCatalogo exercicio, HttpSession session) {
        validarEquipe(session);
        return exercicioCatalogoService.atualizar(id, exercicio);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, HttpSession session) {
        validarEquipe(session);
        exercicioCatalogoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    /** DONO ou INSTRUTOR — mesma regra usada em TreinoController/AlunoController. */
    private void validarEquipe(HttpSession session) {
        Object cargoObj = session.getAttribute("cargo");
        String cargo = cargoObj != null ? cargoObj.toString() : "";
        if (!"DONO".equalsIgnoreCase(cargo) && !"INSTRUTOR".equalsIgnoreCase(cargo)) {
            throw new InstrutorController.AcessoNegadoException(
                    "Apenas DONO ou INSTRUTOR podem gerenciar o catálogo de exercícios.");
        }
    }
}
