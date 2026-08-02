package br.com.gymweb.controller;

import br.com.gymweb.dto.ExercicioTreinoResponseDTO;
import br.com.gymweb.dto.TreinoDTO;
import br.com.gymweb.dto.TreinoResponseDTO;
import br.com.gymweb.service.TreinoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/treinos")
public class TreinoController {

    private final TreinoService treinoService;

    public TreinoController(TreinoService treinoService) {
        this.treinoService = treinoService;
    }

    @GetMapping
    public List<TreinoResponseDTO> listarTodos() {
        return treinoService.listarTodosTreinos();
    }

    @GetMapping("/aluno/{alunoId}")
    public List<TreinoResponseDTO> listarPorAluno(@PathVariable Long alunoId) {
        return treinoService.listarPorAluno(alunoId);
    }

    @GetMapping("/instrutor/{instrutorId}")
    public List<TreinoResponseDTO> listarPorInstrutor(@PathVariable Long instrutorId) {
        return treinoService.listarPorInstrutor(instrutorId);
    }

    @GetMapping("/{id}")
    public TreinoResponseDTO buscarPorId(@PathVariable Long id) {
        return treinoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<TreinoResponseDTO> criar(@RequestBody TreinoDTO treinoDTO, HttpSession session) {
        validarPodeGerenciarTreino(treinoDTO, session);
        TreinoResponseDTO salvo = treinoService.criar(treinoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping("/{id}")
    public TreinoResponseDTO atualizar(@PathVariable Long id, @RequestBody TreinoDTO treinoDTO, HttpSession session) {
        validarPodeGerenciarTreino(treinoDTO, session);
        return treinoService.atualizar(id, treinoDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, HttpSession session) {
        validarSessaoGerencial(session);
        treinoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{treinoId}/exercicios/{exercicioTreinoId}/concluir")
    public ExercicioTreinoResponseDTO marcarConcluido(
            @PathVariable Long treinoId,
            @PathVariable Long exercicioTreinoId,
            @RequestParam(defaultValue = "true") boolean concluido,
            HttpSession session
    ) {
        if (session.getAttribute("usuarioId") == null) {
            throw new InstrutorController.AcessoNegadoException("Faça login para atualizar o checklist.");
        }
        return treinoService.marcarConcluido(treinoId, exercicioTreinoId, concluido);
    }

    private void validarPodeGerenciarTreino(TreinoDTO treinoDTO, HttpSession session) {
        validarSessaoGerencial(session);
        String cargo = (String) session.getAttribute("cargo");
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if ("INSTRUTOR".equalsIgnoreCase(cargo)
                && (treinoDTO.getInstrutorId() == null || !treinoDTO.getInstrutorId().equals(usuarioId))) {
            throw new InstrutorController.AcessoNegadoException("Instrutores só podem gerenciar seus próprios treinos.");
        }
    }

    private void validarSessaoGerencial(HttpSession session) {
        String cargo = (String) session.getAttribute("cargo");
        if (!"DONO".equalsIgnoreCase(cargo) && !"INSTRUTOR".equalsIgnoreCase(cargo)) {
            throw new InstrutorController.AcessoNegadoException("Apenas administrador ou instrutor podem gerenciar treinos.");
        }
    }
}
