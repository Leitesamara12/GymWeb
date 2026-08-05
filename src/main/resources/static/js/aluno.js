let alunosCarregados = [];
let usuarioAtual = null;
let alunoParaExcluir = null;

document.addEventListener("DOMContentLoaded", async () => {
    usuarioAtual = exigirCargo("DONO", "INSTRUTOR");
    if (!usuarioAtual) {
        return;
    }
    configurarCabecalho();

    const formulario = document.querySelector("#form-aluno");
    const campoCpf = document.querySelector("#cpf");
    const seletorInstrutor = document.querySelector("#instrutorId");
    const botaoSalvar = document.querySelector("#btn-salvar");
    const btnLimpar = document.querySelector("#btn-limpar");

    campoCpf.addEventListener("input", () => {
        campoCpf.value = formatarCpf(campoCpf.value);
    });

    // Botão limpar reseta o formulário para modo cadastro
    btnLimpar.addEventListener("click", () => {
        resetarFormulario();
    });

    await carregarInstrutores(usuarioAtual);
    await carregarAlunos(usuarioAtual);

    formulario.addEventListener("submit", async (event) => {
        event.preventDefault();
        const alunoId = document.querySelector("#aluno-id").value;
        const cpf = campoCpf.value.replace(/\D/g, "");
        const senha = document.querySelector("#senha").value;

        if (cpf.length !== 11) {
            exibirMensagem("Informe CPF com 11 números.", "error");
            return;
        }

        const instrutorId = seletorInstrutor.value;
        botaoSalvar.disabled = true;

        try {
            if (alunoId) {
                // EDITAR aluno existente
                const dados = {
                    nome: document.querySelector("#nome").value.trim(),
                    cpf
                };
                // Só envia a senha se o usuário digitou uma nova
                if (senha && senha.length >= 6) {
                    dados.senha = senha;
                }
                await apiPut(`/alunos/${alunoId}`, dados);

                // Atualizar vínculo do instrutor se mudou
                const alunoAtual = alunosCarregados.find(a => String(a.id) === alunoId);
                const vinculoAtual = alunoAtual?.instrutor?.id ? String(alunoAtual.instrutor.id) : "";
                if (vinculoAtual !== (instrutorId || "")) {
                    if (instrutorId) {
                        await apiPut(`/alunos/${alunoId}/instrutor/${instrutorId}`);
                    }
                }

                exibirMensagem("Aluno atualizado com sucesso.", "success");
            } else {
                // CADASTRAR novo aluno
                if (!senha || senha.length < 6) {
                    exibirMensagem("Senha de pelo menos 6 caracteres é obrigatória para novos alunos.", "error");
                    botaoSalvar.disabled = false;
                    return;
                }
                const rota = instrutorId ? `/alunos?instrutorId=${encodeURIComponent(instrutorId)}` : "/alunos";
                await apiPost(rota, {
                    nome: document.querySelector("#nome").value.trim(),
                    cpf,
                    senha
                });
                exibirMensagem("Aluno cadastrado com sucesso.", "success");
            }

            resetarFormulario();
            if (usuarioAtual.cargo === "INSTRUTOR") {
                seletorInstrutor.value = String(usuarioAtual.id);
            }
            await carregarAlunos(usuarioAtual);
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
        if (!alunoParaExcluir) return;

        const btn = btnConfirmar;
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Excluindo...';

        try {
            await apiDelete(`/alunos/${alunoParaExcluir.id}`);
            exibirMensagem(`Aluno "${alunoParaExcluir.nome}" excluído com sucesso.`, "success");
            fecharModal();
            await carregarAlunos(usuarioAtual);
        } catch (error) {
            exibirMensagem(mensagemErro(error), "error");
        } finally {
            btn.disabled = false;
            btn.innerHTML = '<i class="fa-solid fa-trash"></i> Excluir';
            alunoParaExcluir = null;
        }
    });

    // Fechar modal clicando fora
    modal.addEventListener("click", (e) => {
        if (e.target === modal) {
            fecharModal();
        }
    });
}

function abrirModal(aluno) {
    alunoParaExcluir = aluno;
    const modal = document.querySelector("#modal-excluir");
    document.querySelector("#modal-nome-aluno").textContent = aluno.nome;
    modal.classList.remove("hidden");
}

function fecharModal() {
    const modal = document.querySelector("#modal-excluir");
    modal.classList.add("hidden");
    alunoParaExcluir = null;
}

/* ---------- Editar Aluno ---------- */
async function editarAluno(id) {
    const aluno = alunosCarregados.find(a => a.id === id);
    if (!aluno) {
        exibirMensagem("Aluno não encontrado.", "error");
        return;
    }

    // Preencher formulário com os dados do aluno
    document.querySelector("#aluno-id").value = aluno.id;
    document.querySelector("#nome").value = aluno.nome;
    document.querySelector("#cpf").value = formatarCpf(aluno.cpf);
    document.querySelector("#senha").value = "";
    document.querySelector("#senha").setAttribute("placeholder", "Deixe em branco para manter a senha atual");
    document.querySelector("#senha").removeAttribute("required");
    document.querySelector("#senha").removeAttribute("minlength");

    // Selecionar instrutor se houver
    if (aluno.instrutor?.id) {
        document.querySelector("#instrutorId").value = String(aluno.instrutor.id);
    } else {
        document.querySelector("#instrutorId").value = "";
    }

    // Mudar título do formulário
    document.querySelector("#titulo-formulario").innerHTML = '<i class="fa-solid fa-pen-to-square" style="color:var(--neon-green);margin-right:6px;"></i>Editar aluno';

    // Mudar botão
    document.querySelector("#btn-salvar").innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Atualizar aluno';

    // Scroll até o formulário
    document.querySelector("#form-aluno").scrollIntoView({ behavior: "smooth", block: "start" });

    exibirMensagem("Preencha os dados e clique em Atualizar. Deixe a senha em branco para manter a atual.", "info");
}

/* ---------- Resetar Formulário ---------- */
function resetarFormulario() {
    document.querySelector("#aluno-id").value = "";
    document.querySelector("#titulo-formulario").innerHTML = '<i class="fa-solid fa-user-plus" style="color:var(--neon-green);margin-right:6px;"></i>Cadastrar aluno';
    document.querySelector("#btn-salvar").innerHTML = '<i class="fa-solid fa-check"></i> Salvar aluno';
    document.querySelector("#senha").setAttribute("placeholder", "Mínimo 6 caracteres");
    document.querySelector("#senha").setAttribute("required", "");
    document.querySelector("#senha").setAttribute("minlength", "6");
}

/* ---------- Carregar Instrutores ---------- */
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

/* ---------- Carregar Alunos ---------- */
async function carregarAlunos(usuario) {
    const destino = document.querySelector("#lista-alunos");
    try {
        const rota = usuario.cargo === "INSTRUTOR" ? `/alunos/instrutor/${usuario.id}` : "/alunos";
        const alunos = await apiGet(rota);
        alunosCarregados = alunos || [];

        if (!alunosCarregados.length) {
            destino.innerHTML = '<p class="empty"><i class="fa-solid fa-inbox" style="font-size:1.5rem;margin-bottom:0.5rem;display:block;opacity:0.4;"></i>Nenhum aluno cadastrado.</p>';
            return;
        }

        const isDono = usuario.cargo === "DONO";
        const actionColumn = isDono ? '<th style="text-align:right;">Ações</th>' : '';

        destino.innerHTML = `
            <table>
                <thead>
                    <tr>
                        <th><i class="fa-solid fa-user" style="margin-right:4px;"></i>Nome</th>
                        <th><i class="fa-solid fa-id-card" style="margin-right:4px;"></i>CPF</th>
                        <th><i class="fa-solid fa-chalkboard-user" style="margin-right:4px;"></i>Instrutor responsável</th>
                        <th><i class="fa-solid fa-shield-halved" style="margin-right:4px;"></i>Cargo</th>
                        ${actionColumn}
                    </tr>
                </thead>
                <tbody>${alunosCarregados.map((aluno) => `
                    <tr>
                        <td>
                            <div style="display:flex;align-items:center;gap:0.5rem;">
                                <span class="avatar-sm" style="display:inline-flex;align-items:center;justify-content:center;width:28px;height:28px;border-radius:50%;background:rgba(200,247,37,0.08);border:1px solid rgba(200,247,37,0.2);color:var(--neon-green);font-size:0.65rem;font-weight:700;flex-shrink:0;">${escaparHtml((aluno.nome || "?").split(" ").map(n => n[0]).slice(0, 2).join("").toUpperCase())}</span>
                                ${escaparHtml(aluno.nome)}
                            </div>
                        </td>
                        <td>${formatarCpf(aluno.cpf)}</td>
                        <td>${escaparHtml(aluno.instrutor?.nome || "Não vinculado")}</td>
                        <td><span style="background:rgba(200,247,37,0.08);color:var(--neon-green);padding:0.2rem 0.6rem;border-radius:50px;font-size:0.72rem;font-weight:700;border:1px solid rgba(200,247,37,0.2);">${escaparHtml(aluno.cargo)}</span></td>
                        ${isDono ? `<td style="text-align:right;">
                            <button class="btn-acao btn-acao-editar" onclick="editarAluno(${aluno.id})" title="Editar aluno">
                                <i class="fa-solid fa-pen"></i>
                            </button>
                            <button class="btn-acao btn-acao-excluir" onclick="abrirModal({id:${aluno.id},nome:'${escaparHtml(aluno.nome).replace(/'/g, "\\'")}'})" title="Excluir aluno">
                                <i class="fa-solid fa-trash"></i>
                            </button>
                        </td>` : ''}
                    </tr>`).join("")}</tbody>
            </table>`;
    } catch (error) {
        destino.innerHTML = `<p class="empty">${escaparHtml(mensagemErro(error))}</p>`;
    }
}

/* ---------- Funções globais ---------- */
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
