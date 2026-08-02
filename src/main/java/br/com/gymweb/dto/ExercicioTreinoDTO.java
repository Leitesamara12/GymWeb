package br.com.gymweb.dto;

/**
 * Item de exercício usado dentro de TreinoDTO ao criar ou editar um treino.
 */
public class ExercicioTreinoDTO {

    private Long exercicioCatalogoId;
    private Integer series;
    private Integer repeticoes;
    private Double carga;
    private Integer ordem;

    public Long getExercicioCatalogoId() {
        return exercicioCatalogoId;
    }

    public void setExercicioCatalogoId(Long exercicioCatalogoId) {
        this.exercicioCatalogoId = exercicioCatalogoId;
    }

    public Integer getSeries() {
        return series;
    }

    public void setSeries(Integer series) {
        this.series = series;
    }

    public Integer getRepeticoes() {
        return repeticoes;
    }

    public void setRepeticoes(Integer repeticoes) {
        this.repeticoes = repeticoes;
    }

    public Double getCarga() {
        return carga;
    }

    public void setCarga(Double carga) {
        this.carga = carga;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }
}
