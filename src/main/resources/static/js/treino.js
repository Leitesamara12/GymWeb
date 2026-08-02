let exerciciosDoCatalogo = [];

document.addEventListener("DOMContentLoaded", async () => {
    const usuario = exigirCargo("DONO", "INSTRUTOR");
    if (!usuario) {
        return;
    }
    configurarCabecalho();

    document.querySelector("#btn-adicionar-exercicio").addEventListener("click", adicionarLinhaExercicio);
    document.querySelector("#form-treino").addEventListener("submit", salvarTreino);

    try {
        await carregarDadosIniciais(usuario);
        adicionarLinhaExercicio();
    } catch (error) {
        exibirMensagem(mensagemErro(error), "error");
    }
});

async function carregarDadosIniciais(usuario) {
    const seletorAluno = document.querySelector("#alunoId");
    const seletorInstrutor = document.querySelector("#instrutorId");

    const promessas = [
        usuario.cargo === "INSTRUTOR" ? apiGet(`/alunos/instrutor/${usuario.id}`) : apiGet("/alunos"),
        apiGet("/exercicios")
    ];
    if (usuario.cargo === "DONO") {
        promessas.push(apiGet("/instrutores"));
    }

    const resultados = await Promise.all(promessas);
    const alunos = resultados[0];
    exerciciosDoCatalogo = resultados[1];
    const instrutores = usuario.cargo === "DONO" ? resultados[2] : [{ id: usuario.id, nome: usuario.nome }];

    preencherOpcoes(seletorAluno, alunos, "Selecione o aluno");
    preencherOpcoes(seletorInstrutor, instrutores, "Selecione o instrutor");
    if (usuario.cargo === "INSTRUTOR") {
        seletorInstrutor.value = String(usuario.id);
    }

    if (!alunos.length) {
        exibirMensagem("Cadastre ao menos um aluno antes de montar um treino.", "info");
    }
    if (!exerciciosDoCatalogo.length) {
        exibirMensagem("Cadastre ao menos um exercício no catálogo antes de montar um treino.", "info");
    }
}

function preencherOpcoes(seletor, itens, textoPadrao) {
    seletor.innerHTML = `<option value="">${textoPadrao}</option>`;
    itens.forEach((item) => {
        seletor.insertAdjacentHTML("beforeend", `<option value="${item.id}">${escaparHtml(item.nome)}</option>`);
    });
}

function adicionarLinhaExercicio() {
    if (!exerciciosDoCatalogo.length) {
        exibirMensagem("Não há exercícios disponíveis no catálogo.", "error");
        return;
    }

    const fragmento = document.querySelector("#template-exercicio").content.cloneNode(true);
    const linha = fragmento.querySelector(".exercise-row");
    const seletor = linha.querySelector(".campo-exercicio");
    preencherOpcoes(seletor, exerciciosDoCatalogo, "Selecione o exercício");
    linha.querySelector(".btn-remover-exercicio").addEventListener("click", () => {
        const linhas = document.querySelectorAll("#linhas-exercicios .exercise-row");
        if (linhas.length <= 1) {
            exibirMensagem("O treino deve conter pelo menos um exercício.", "error");
            return;
        }
        linha.remove();
    });
    document.querySelector("#linhas-exercicios").appendChild(fragmento);
}

async function salvarTreino(event) {
    event.preventDefault();
    const linhas = [...document.querySelectorAll("#linhas-exercicios .exercise-row")];
    if (!linhas.length) {
        exibirMensagem("Inclua ao menos um exercício.", "error");
        return;
    }

    const exercicios = [];
    for (let indice = 0; indice < linhas.length; indice += 1) {
        const linha = linhas[indice];
        const exercicioCatalogoId = Number(linha.querySelector(".campo-exercicio").value);
        const series = Number(linha.querySelector(".campo-series").value);
        const repeticoes = Number(linha.querySelector(".campo-repeticoes").value);
        const carga = Number(linha.querySelector(".campo-carga").value);
        if (!exercicioCatalogoId || series < 1 || repeticoes < 1 || carga < 0) {
            exibirMensagem("Preencha todos os exercícios com valores válidos.", "error");
            return;
        }
        exercicios.push({ exercicioCatalogoId, series, repeticoes, carga, ordem: indice + 1 });
    }

    const payload = {
        nome: document.querySelector("#nome").value.trim(),
        descricao: document.querySelector("#descricao").value.trim(),
        alunoId: Number(document.querySelector("#alunoId").value),
        instrutorId: Number(document.querySelector("#instrutorId").value),
        exercicios
    };

    if (!payload.nome || !payload.alunoId || !payload.instrutorId) {
        exibirMensagem("Preencha nome, aluno e instrutor do treino.", "error");
        return;
    }

    const botao = document.querySelector("#btn-salvar");
    botao.disabled = true;
    try {
        await apiPost("/treinos", payload);
        exibirMensagem("Treino salvo com sucesso.", "success");
        setTimeout(() => window.location.assign("/dashboard.html"), 700);
    } catch (error) {
        exibirMensagem(mensagemErro(error), "error");
    } finally {
        botao.disabled = false;
    }
}

function exibirMensagem(texto, tipo) {
    const mensagem = document.querySelector("#mensagem");
    mensagem.textContent = texto;
    mensagem.className = `notice ${tipo}`;
}

function escaparHtml(valor) {
    return String(valor ?? "").replace(/[&<>'"]/g, (caractere) => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", "\"": "&quot;"
    })[caractere]);
}
