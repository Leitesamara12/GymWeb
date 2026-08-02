document.addEventListener("DOMContentLoaded", async () => {
    const usuario = exigirCargo("ALUNO");
    if (!usuario) {
        return;
    }
    configurarCabecalho();

    try {
        const treinos = await apiGet(`/treinos/aluno/${usuario.id}`);
        if (!treinos.length) {
            document.querySelector("#treino-detalhe").innerHTML = "<p class=\"empty\">Nenhum treino foi atribuído a você ainda.</p>";
            return;
        }
        renderizarTreino(treinos[treinos.length - 1]);
    } catch (error) {
        exibirMensagem(mensagemErro(error), "error");
        document.querySelector("#treino-detalhe").innerHTML = "<p class=\"empty\">Não foi possível carregar o seu treino.</p>";
    }
});

function renderizarTreino(treino) {
    const exercicios = [...(treino.exercicios || [])].sort((a, b) => a.ordem - b.ordem);
    document.querySelector("#treino-detalhe").innerHTML = `
        <h2 style="margin-top:0;">${escaparHtml(treino.nome || "Meu treino")}</h2>
        <p class="status">${escaparHtml(treino.descricao || "Siga a sequência abaixo e marque os itens concluídos.")}</p>
        <p class="status" id="progresso"></p>`;

    const lista = document.querySelector("#lista-checklist");
    if (!exercicios.length) {
        lista.innerHTML = "<p class=\"empty\">Este treino ainda não possui exercícios.</p>";
        return;
    }

    lista.innerHTML = "";
    exercicios.forEach((exercicioTreino) => {
        lista.appendChild(criarItemChecklist(treino.id, exercicioTreino));
    });
    atualizarProgresso();
}

function criarItemChecklist(treinoId, exercicioTreino) {
    const exercicio = exercicioTreino.exercicio || {};
    const item = document.createElement("label");
    item.className = `check-item ${exercicioTreino.concluido ? "done" : ""}`;
    item.innerHTML = `
        <input type="checkbox" ${exercicioTreino.concluido ? "checked" : ""}>
        <span>
            <strong>${escaparHtml(exercicio.nome || "Exercício")}</strong>
            <span>${exercicioTreino.series} séries · ${exercicioTreino.repeticoes} repetições · ${exercicioTreino.carga} kg</span>
        </span>`;

    const checkbox = item.querySelector("input");
    checkbox.addEventListener("change", async () => {
        checkbox.disabled = true;
        try {
            await apiPatch(`/treinos/${treinoId}/exercicios/${exercicioTreino.id}/concluir?concluido=${checkbox.checked}`);
            item.classList.toggle("done", checkbox.checked);
            atualizarProgresso();
        } catch (error) {
            checkbox.checked = !checkbox.checked;
            exibirMensagem(mensagemErro(error), "error");
        } finally {
            checkbox.disabled = false;
        }
    });
    return item;
}

function atualizarProgresso() {
    const marcados = [...document.querySelectorAll("#lista-checklist input[type=checkbox]")];
    const concluidos = marcados.filter((checkbox) => checkbox.checked).length;
    const progresso = document.querySelector("#progresso");
    progresso.textContent = `${concluidos} de ${marcados.length} exercícios concluídos.`;
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
