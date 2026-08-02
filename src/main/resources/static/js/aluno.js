document.addEventListener("DOMContentLoaded", async () => {
    const usuario = exigirCargo("DONO", "INSTRUTOR");
    if (!usuario) {
        return;
    }
    configurarCabecalho();

    const formulario = document.querySelector("#form-aluno");
    const campoCpf = document.querySelector("#cpf");
    const seletorInstrutor = document.querySelector("#instrutorId");
    const botaoSalvar = document.querySelector("#btn-salvar");

    campoCpf.addEventListener("input", () => {
        campoCpf.value = formatarCpf(campoCpf.value);
    });

    await carregarInstrutores(usuario);
    await carregarAlunos(usuario);

    formulario.addEventListener("submit", async (event) => {
        event.preventDefault();
        const cpf = campoCpf.value.replace(/\D/g, "");
        const senha = document.querySelector("#senha").value;
        if (cpf.length !== 11 || senha.length < 6) {
            exibirMensagem("Informe CPF com 11 números e senha de pelo menos 6 caracteres.", "error");
            return;
        }

        const instrutorId = seletorInstrutor.value;
        const rota = instrutorId ? `/alunos?instrutorId=${encodeURIComponent(instrutorId)}` : "/alunos";
        botaoSalvar.disabled = true;
        try {
            await apiPost(rota, {
                nome: document.querySelector("#nome").value.trim(),
                cpf,
                senha
            });
            formulario.reset();
            if (usuario.cargo === "INSTRUTOR") {
                seletorInstrutor.value = String(usuario.id);
            }
            exibirMensagem("Aluno cadastrado com sucesso.", "success");
            await carregarAlunos(usuario);
        } catch (error) {
            exibirMensagem(mensagemErro(error), "error");
        } finally {
            botaoSalvar.disabled = false;
        }
    });
});

async function carregarInstrutores(usuario) {
    const seletor = document.querySelector("#instrutorId");
    seletor.innerHTML = "<option value=\"\">Sem vínculo inicial</option>";

    if (usuario.cargo === "INSTRUTOR") {
        seletor.insertAdjacentHTML("beforeend", `<option value="${usuario.id}">${escaparHtml(usuario.nome)} (você)</option>`);
        seletor.value = String(usuario.id);
        return;
    }

    try {
        const instrutores = await apiGet("/instrutores");
        instrutores.forEach((instrutor) => {
            seletor.insertAdjacentHTML(
                "beforeend",
                `<option value="${instrutor.id}">${escaparHtml(instrutor.nome)} — ${escaparHtml(instrutor.especialidade || "Sem especialidade")}</option>`
            );
        });
    } catch (error) {
        exibirMensagem(`Não foi possível carregar os instrutores: ${mensagemErro(error)}`, "error");
    }
}

async function carregarAlunos(usuario) {
    const destino = document.querySelector("#lista-alunos");
    try {
        const rota = usuario.cargo === "INSTRUTOR" ? `/alunos/instrutor/${usuario.id}` : "/alunos";
        const alunos = await apiGet(rota);
        if (!alunos.length) {
            destino.innerHTML = "<p class=\"empty\">Nenhum aluno cadastrado.</p>";
            return;
        }
        destino.innerHTML = `
            <table>
                <thead><tr><th>Nome</th><th>CPF</th><th>Instrutor responsável</th><th>Cargo</th></tr></thead>
                <tbody>${alunos.map((aluno) => `
                    <tr>
                        <td>${escaparHtml(aluno.nome)}</td>
                        <td>${formatarCpf(aluno.cpf)}</td>
                        <td>${escaparHtml(aluno.instrutor?.nome || "Não vinculado")}</td>
                        <td>${escaparHtml(aluno.cargo)}</td>
                    </tr>`).join("")}</tbody>
            </table>`;
    } catch (error) {
        destino.innerHTML = `<p class="empty">${escaparHtml(mensagemErro(error))}</p>`;
    }
}

function listarAlunos() {
    return apiGet("/alunos");
}

function listarAlunosPorInstrutor(instrutorId) {
    return apiGet(`/alunos/instrutor/${instrutorId}`);
}

function vincularAlunoAoInstrutor(alunoId, instrutorId) {
    return apiPut(`/alunos/${alunoId}/instrutor/${instrutorId}`);
}

function formatarCpf(valor) {
    const numeros = String(valor || "").replace(/\D/g, "").slice(0, 11);
    return numeros
        .replace(/(\d{3})(\d)/, "$1.$2")
        .replace(/(\d{3})(\d)/, "$1.$2")
        .replace(/(\d{3})(\d{1,2})$/, "$1-$2");
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
