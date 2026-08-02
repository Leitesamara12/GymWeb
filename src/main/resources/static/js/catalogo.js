let exerciciosCarregados = [];

document.addEventListener("DOMContentLoaded", async () => {
    const usuario = exigirCargo("DONO", "INSTRUTOR");
    if (!usuario) {
        return;
    }
    configurarCabecalho();

    document.querySelector("#form-exercicio").addEventListener("submit", salvarExercicio);
    document.querySelector("#filtro").addEventListener("input", renderizarExercicios);
    await carregarExercicios();
});

async function salvarExercicio(event) {
    event.preventDefault();
    try {
        await apiPost("/exercicios", {
            nome: document.querySelector("#nome").value.trim(),
            grupoMuscular: document.querySelector("#grupoMuscular").value.trim(),
            descricao: document.querySelector("#descricao").value.trim()
        });
        event.currentTarget.reset();
        exibirMensagem("Exercício cadastrado com sucesso.", "success");
        await carregarExercicios();
    } catch (error) {
        exibirMensagem(mensagemErro(error), "error");
    }
}

async function carregarExercicios() {
    const destino = document.querySelector("#lista-exercicios");
    try {
        exerciciosCarregados = await apiGet("/exercicios");
        renderizarExercicios();
    } catch (error) {
        destino.innerHTML = `<p class="empty">${escaparHtml(mensagemErro(error))}</p>`;
    }
}

function renderizarExercicios() {
    const destino = document.querySelector("#lista-exercicios");
    const termo = document.querySelector("#filtro").value.trim().toLowerCase();
    const filtrados = exerciciosCarregados.filter((exercicio) => {
        const texto = `${exercicio.nome || ""} ${exercicio.grupoMuscular || ""} ${exercicio.descricao || ""}`.toLowerCase();
        return texto.includes(termo);
    });

    if (!filtrados.length) {
        destino.innerHTML = "<p class=\"empty\">Nenhum exercício encontrado.</p>";
        return;
    }

    destino.innerHTML = `
        <table>
            <thead><tr><th>Exercício</th><th>Grupo muscular</th><th>Descrição</th><th>Ação</th></tr></thead>
            <tbody>${filtrados.map((exercicio) => `
                <tr>
                    <td>${escaparHtml(exercicio.nome)}</td>
                    <td>${escaparHtml(exercicio.grupoMuscular || "—")}</td>
                    <td>${escaparHtml(exercicio.descricao || "—")}</td>
                    <td><button class="btn btn-danger" type="button" onclick="excluirExercicio(${exercicio.id})">Excluir</button></td>
                </tr>`).join("")}</tbody>
        </table>`;
}

async function excluirExercicio(id) {
    if (!window.confirm("Deseja excluir este exercício do catálogo?")) {
        return;
    }
    try {
        await apiDelete(`/exercicios/${id}`);
        exibirMensagem("Exercício excluído com sucesso.", "success");
        await carregarExercicios();
    } catch (error) {
        exibirMensagem(mensagemErro(error), "error");
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
