let instrutoresCarregados = [];
let instrutorParaExcluir = null;

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
    const btnLimpar = document.querySelector("#btn-limpar");

    campoCpf.addEventListener("input", () => {
        campoCpf.value = formatarCpf(campoCpf.value);
    });
    campoTelefone.addEventListener("input", () => {
        campoTelefone.value = formatarTelefone(campoTelefone.value);
    });

    // Botão limpar reseta o formulário para modo cadastro
    btnLimpar.addEventListener("click", () => {
        resetarFormulario();
    });

    await carregarInstrutores();

    formulario.addEventListener("submit", async (event) => {
        event.preventDefault();
        const instrutorId = document.querySelector("#instrutor-id").value;
        const cpf = campoCpf.value.replace(/\D/g, "");
        const senha = document.querySelector("#senha").value;

        if (cpf.length !== 11) {
            exibirMensagem("Informe CPF com 11 números.", "error");
            return;
        }

        botaoSalvar.disabled = true;
        try {
            if (instrutorId) {
                // EDITAR instrutor existente
                const dados = {
                    nome: document.querySelector("#nome").value.trim(),
                    cpf,
                    email: document.querySelector("#email").value.trim(),
                    telefone: campoTelefone.value.replace(/\D/g, ""),
                    especialidade: document.querySelector("#especialidade").value.trim()
                };
                // Só envia a senha se o usuário digitou uma nova
                if (senha && senha.length >= 6) {
                    dados.senha = senha;
                }
                await apiPut(`/instrutores/${instrutorId}`, dados);
                exibirMensagem("Instrutor atualizado com sucesso.", "success");
            } else {
                // CADASTRAR novo instrutor
                if (!senha || senha.length < 6) {
                    exibirMensagem("Senha de pelo menos 6 caracteres é obrigatória.", "error");
                    botaoSalvar.disabled = false;
                    return;
                }
                await apiPost("/instrutores", {
                    nome: document.querySelector("#nome").value.trim(),
                    cpf,
                    email: document.querySelector("#email").value.trim(),
                    telefone: campoTelefone.value.replace(/\D/g, ""),
                    especialidade: document.querySelector("#especialidade").value.trim(),
                    senha
                });
                exibirMensagem("Instrutor cadastrado com sucesso.", "success");
            }

            resetarFormulario();
            await carregarInstrutores();
        } catch (error) {
            exibirMensagem(mensagemErro(error), "error");
        } finally {
            botaoSalvar.disabled = false;
        }
    });

    // Configurar modal de exclusão
    configurarModal();
});

/* ---------- Modal de Exclusão ---------- */
function configurarModal() {
    const modal = document.querySelector("#modal-excluir");
    const btnFechar = document.querySelector("#modal-fechar");
    const btnCancelar = document.querySelector("#modal-cancelar");
    const btnConfirmar = document.querySelector("#modal-confirmar");

    btnFechar.addEventListener("click", () => {
        fecharModal();
    });

    btnCancelar.addEventListener("click", () => {
        fecharModal();
    });

    btnConfirmar.addEventListener("click", async () => {
        if (!instrutorParaExcluir) return;

        const btn = btnConfirmar;
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Excluindo...';

        try {
            await apiDelete(`/instrutores/${instrutorParaExcluir.id}`);
            exibirMensagem(`Instrutor "${instrutorParaExcluir.nome}" excluído com sucesso.`, "success");
            fecharModal();
            await carregarInstrutores();
        } catch (error) {
            exibirMensagem(mensagemErro(error), "error");
        } finally {
            btn.disabled = false;
            btn.innerHTML = '<i class="fa-solid fa-trash"></i> Excluir';
            instrutorParaExcluir = null;
        }
    });

    // Fechar modal clicando fora
    modal.addEventListener("click", (e) => {
        if (e.target === modal) {
            fecharModal();
        }
    });
}

function abrirModal(instrutor) {
    instrutorParaExcluir = instrutor;
    const modal = document.querySelector("#modal-excluir");
    document.querySelector("#modal-nome-instrutor").textContent = instrutor.nome;
    modal.classList.remove("hidden");
}

function fecharModal() {
    const modal = document.querySelector("#modal-excluir");
    modal.classList.add("hidden");
    instrutorParaExcluir = null;
}

/* ---------- Editar Instrutor ---------- */
async function editarInstrutor(id) {
    const instrutor = instrutoresCarregados.find(i => i.id === id);
    if (!instrutor) {
        exibirMensagem("Instrutor não encontrado.", "error");
        return;
    }

    // Preencher formulário com os dados do instrutor
    document.querySelector("#instrutor-id").value = instrutor.id;
    document.querySelector("#nome").value = instrutor.nome;
    document.querySelector("#cpf").value = formatarCpf(instrutor.cpf);
    document.querySelector("#email").value = instrutor.email || "";
    document.querySelector("#telefone").value = instrutor.telefone ? formatarTelefone(instrutor.telefone) : "";
    document.querySelector("#especialidade").value = instrutor.especialidade || "";
    document.querySelector("#senha").value = "";
    document.querySelector("#senha").setAttribute("placeholder", "Deixe em branco para manter a senha atual");
    document.querySelector("#senha").removeAttribute("required");
    document.querySelector("#senha").removeAttribute("minlength");

    // Mudar título do formulário
    document.querySelector("#titulo-formulario").innerHTML = '<i class="fa-solid fa-pen-to-square" style="color:var(--neon-green);margin-right:6px;"></i>Editar instrutor';

    // Mudar botão
    document.querySelector("#btn-salvar").innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Atualizar instrutor';

    // Scroll até o formulário
    document.querySelector("#form-instrutor").scrollIntoView({ behavior: "smooth", block: "start" });

    exibirMensagem("Preencha os dados e clique em Atualizar. Deixe a senha em branco para manter a atual.", "info");
}

/* ---------- Resetar Formulário ---------- */
function resetarFormulario() {
    document.querySelector("#instrutor-id").value = "";
    document.querySelector("#titulo-formulario").innerHTML = '<i class="fa-solid fa-user-plus" style="color:var(--neon-green);margin-right:6px;"></i>Cadastrar instrutor';
    document.querySelector("#btn-salvar").innerHTML = '<i class="fa-solid fa-check"></i> Salvar instrutor';
    document.querySelector("#senha").setAttribute("placeholder", "Mínimo 6 caracteres");
    document.querySelector("#senha").setAttribute("required", "");
    document.querySelector("#senha").setAttribute("minlength", "6");
}

/* ---------- Carregar Instrutores ---------- */
async function carregarInstrutores() {
    const destino = document.querySelector("#lista-instrutores");
    try {
        const instrutores = await apiGet("/instrutores");
        instrutoresCarregados = instrutores || [];

        if (!instrutoresCarregados.length) {
            destino.innerHTML = '<p class="empty"><i class="fa-solid fa-inbox" style="font-size:1.5rem;margin-bottom:0.5rem;display:block;opacity:0.4;"></i>Nenhum instrutor cadastrado.</p>';
            return;
        }

        destino.innerHTML = `
            <table>
                <thead>
                    <tr>
                        <th><i class="fa-solid fa-user" style="margin-right:4px;"></i>Nome</th>
                        <th><i class="fa-solid fa-id-card" style="margin-right:4px;"></i>CPF</th>
                        <th><i class="fa-solid fa-envelope" style="margin-right:4px;"></i>E-mail</th>
                        <th><i class="fa-solid fa-dumbbell" style="margin-right:4px;"></i>Especialidade</th>
                        <th><i class="fa-solid fa-shield-halved" style="margin-right:4px;"></i>Cargo</th>
                        <th style="text-align:right;">Ações</th>
                    </tr>
                </thead>
                <tbody>${instrutoresCarregados.map((instrutor) => `
                    <tr>
                        <td>
                            <div style="display:flex;align-items:center;gap:0.5rem;">
                                <span class="avatar-sm" style="display:inline-flex;align-items:center;justify-content:center;width:28px;height:28px;border-radius:50%;background:rgba(200,247,37,0.08);border:1px solid rgba(200,247,37,0.2);color:var(--neon-green);font-size:0.65rem;font-weight:700;flex-shrink:0;">${escaparHtml((instrutor.nome || "?").split(" ").map(n => n[0]).slice(0, 2).join("").toUpperCase())}</span>
                                ${escaparHtml(instrutor.nome)}
                            </div>
                        </td>
                        <td>${formatarCpf(instrutor.cpf)}</td>
                        <td>${escaparHtml(instrutor.email)}</td>
                        <td>${escaparHtml(instrutor.especialidade || "—")}</td>
                        <td><span style="background:rgba(200,247,37,0.08);color:var(--neon-green);padding:0.2rem 0.6rem;border-radius:50px;font-size:0.72rem;font-weight:700;border:1px solid rgba(200,247,37,0.2);">${escaparHtml(instrutor.cargo)}</span></td>
                        <td style="text-align:right;">
                            <button class="btn-acao btn-acao-editar" onclick="editarInstrutor(${instrutor.id})" title="Editar instrutor">
                                <i class="fa-solid fa-pen"></i>
                            </button>
                            <button class="btn-acao btn-acao-excluir" onclick="abrirModal({id:${instrutor.id},nome:'${escaparHtml(instrutor.nome).replace(/'/g, "\\'")}'})" title="Excluir instrutor">
                                <i class="fa-solid fa-trash"></i>
                            </button>
                        </td>
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
