document.addEventListener("DOMContentLoaded", async () => {
    const usuario = exigirCargo("DONO");
    if (!usuario) {
        return;
    }
    configurarCabecalho();

    const formulario = document.querySelector("#form-instrutor");
    const campoCpf = document.querySelector("#cpf");
    const campoTelefone = document.querySelector("#telefone");
    const botaoSalvar = document.querySelector("#btn-salvar");

    campoCpf.addEventListener("input", () => {
        campoCpf.value = formatarCpf(campoCpf.value);
    });
    campoTelefone.addEventListener("input", () => {
        campoTelefone.value = formatarTelefone(campoTelefone.value);
    });

    formulario.addEventListener("submit", async (event) => {
        event.preventDefault();
        const cpf = campoCpf.value.replace(/\D/g, "");
        const senha = document.querySelector("#senha").value;
        if (cpf.length !== 11 || senha.length < 6) {
            exibirMensagem("Informe CPF com 11 números e senha de pelo menos 6 caracteres.", "error");
            return;
        }

        botaoSalvar.disabled = true;
        try {
            await apiPost("/instrutores", {
                nome: document.querySelector("#nome").value.trim(),
                cpf,
                email: document.querySelector("#email").value.trim(),
                telefone: campoTelefone.value.replace(/\D/g, ""),
                especialidade: document.querySelector("#especialidade").value.trim(),
                senha
            });
            formulario.reset();
            exibirMensagem("Instrutor cadastrado com sucesso.", "success");
            await carregarInstrutores();
        } catch (error) {
            exibirMensagem(mensagemErro(error), "error");
        } finally {
            botaoSalvar.disabled = false;
        }
    });

    await carregarInstrutores();
});

async function carregarInstrutores() {
    const destino = document.querySelector("#lista-instrutores");
    try {
        const instrutores = await apiGet("/instrutores");
        if (!instrutores.length) {
            destino.innerHTML = "<p class=\"empty\">Nenhum instrutor cadastrado.</p>";
            return;
        }
        destino.innerHTML = `
            <table>
                <thead><tr><th>Nome</th><th>CPF</th><th>E-mail</th><th>Especialidade</th><th>Cargo</th></tr></thead>
                <tbody>${instrutores.map((instrutor) => `
                    <tr>
                        <td>${escaparHtml(instrutor.nome)}</td>
                        <td>${formatarCpf(instrutor.cpf)}</td>
                        <td>${escaparHtml(instrutor.email)}</td>
                        <td>${escaparHtml(instrutor.especialidade || "—")}</td>
                        <td>${escaparHtml(instrutor.cargo)}</td>
                    </tr>`).join("")}</tbody>
            </table>`;
    } catch (error) {
        destino.innerHTML = `<p class="empty">${escaparHtml(mensagemErro(error))}</p>`;
    }
}

function formatarCpf(valor) {
    const numeros = String(valor || "").replace(/\D/g, "").slice(0, 11);
    return numeros
        .replace(/(\d{3})(\d)/, "$1.$2")
        .replace(/(\d{3})(\d)/, "$1.$2")
        .replace(/(\d{3})(\d{1,2})$/, "$1-$2");
}

function formatarTelefone(valor) {
    const numeros = String(valor || "").replace(/\D/g, "").slice(0, 11);
    return numeros
        .replace(/^(\d{2})(\d)/, "($1) $2")
        .replace(/(\d{5})(\d)/, "$1-$2");
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
