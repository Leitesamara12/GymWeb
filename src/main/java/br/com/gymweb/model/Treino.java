package br.com.gymweb.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "treino")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Treino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do treino é obrigatório.")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nome;

    @Size(max = 500)
    @Column(length = 500)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrutor_id", nullable = false)
    private Instrutor instrutor;

    @OneToMany(
            mappedBy = "treino",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ExercicioTreino> exercicios = new ArrayList<>();

    /**
     * Adiciona um exercício ao treino.
     */
    public ExercicioTreino addExercicio(ExercicioCatalogo catalogo,
                                        Integer ordem,
                                        Integer series,
                                        Integer repeticoes,
                                        Double carga) {

        ExercicioTreino exercicioTreino = new ExercicioTreino();

        exercicioTreino.setTreino(this);
        exercicioTreino.setExercicio(catalogo);
        exercicioTreino.setOrdem(ordem);
        exercicioTreino.setSeries(series);
        exercicioTreino.setRepeticoes(repeticoes);
        exercicioTreino.setCarga(carga);
        exercicioTreino.setConcluido(false);

        this.exercicios.add(exercicioTreino);

        return exercicioTreino;
    }

    /**
     * Adiciona um exercício já existente.
     */
    public void addExercicio(ExercicioTreino exercicioTreino) {

        if (exercicioTreino == null) {
            return;
        }

        exercicioTreino.setTreino(this);

        if (!this.exercicios.contains(exercicioTreino)) {
            this.exercicios.add(exercicioTreino);
        }
    }

    /**
     * Remove um exercício do treino.
     */
    public void removeExercicio(ExercicioTreino exercicioTreino) {

        if (exercicioTreino == null) {
            return;
        }

        this.exercicios.remove(exercicioTreino);
        exercicioTreino.setTreino(null);
    }

}