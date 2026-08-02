document.addEventListener("DOMContentLoaded", async () => {
    const usuario = exigirLogin();
    if (!usuario) {
        return;
    }

    configurarCabecalho();
    document.querySelector("#cargo-usuario").textContent = usuario.cargo;

    try {
        const [alunos, treinos, instrutores] = await Promise.all([
            apiGet("/alunos"),
            usuario.cargo === "ALUNO" ? apiGet(`/treinos/aluno/${usuario.id}`) : apiGet("/treinos"),
            apiGet("/instrutores/contar").catch(() => ({ totalInstrutores: null }))
        ]);

        document.querySelector("#total-alunos").textContent = Array.isArray(alunos) ? alunos.length : 0;
        document.querySelector("#total-treinos").textContent = Array.isArray(treinos) ? treinos.length : 0;
        document.querySelector("#total-instrutores").textContent = instrutores.totalInstrutores ?? "—";
        renderizarTreinos(treinos || []);
    } catch (error) {
        exibirMensagem(mensagemErro(error), "error");
        document.querySelector("#lista-treinos").innerHTML = "<p class=\"empty\">Não foi possível carregar os dados do painel.</p>";
    }
});

function renderizarTreinos(treinos) {
    const destino = document.querySelector("#lista-treinos");
    if (!treinos.length) {
        destino.innerHTML = "<p class=\"empty\">Nenhum treino cadastrado até o momento.</p>";
        return;
    }

    const linhas = treinos.slice(0, 8).map((treino) => `
        <tr>
            <td>${escaparHtml(treino.nome || "Sem nome")}</td>
            <td>${escaparHtml(treino.aluno?.nome || "—")}</td>
            <td>${escaparHtml(treino.instrutor?.nome || "—")}</td>
            <td>${Array.isArray(treino.exercicios) ? treino.exercicios.length : 0}</td>
        </tr>
    `).join("");

    destino.innerHTML = `
        <table>
            <thead><tr><th>Treino</th><th>Aluno</th><th>Instrutor</th><th>Exercícios</th></tr></thead>
            <tbody>${linhas}</tbody>
        </table>`;
}

function exibirMensagem(texto, tipo) {
    const mensagem = document.querySelector("#mensagem");
    mensagem.textContent = texto;
    mensagem.className = `notice ${tipo}`;
}

function escaparHtml(valor) {
    return String(valor).replace(/[&<>'"]/g, (caractere) => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        "'": "&#39;",
        "\"": "&quot;"
    })[caractere]);
}
