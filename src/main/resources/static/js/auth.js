const CHAVE_SESSAO = "gymweb_usuario";

function salvarSessao(usuario) {
    localStorage.setItem(CHAVE_SESSAO, JSON.stringify(usuario));
}

function getUsuarioLogado() {
    try {
        const dados = localStorage.getItem(CHAVE_SESSAO);
        return dados ? JSON.parse(dados) : null;
    } catch {
        localStorage.removeItem(CHAVE_SESSAO);
        return null;
    }
}

function limparSessao() {
    localStorage.removeItem(CHAVE_SESSAO);
}

function paginaInicialDoUsuario(usuario) {
    return usuario?.cargo === "ALUNO" ? "/checklist.treino.html" : "/dashboard.html";
}

async function logout() {
    try {
        await apiRequest("/auth/logout", { method: "POST" });
    } catch {
        // A limpeza local ainda deve ocorrer quando o servidor não estiver disponível.
    }
    limparSessao();
    window.location.assign("/login.html");
}

function exigirLogin() {
    const usuario = getUsuarioLogado();
    if (!usuario) {
        window.location.replace("/login.html");
        return null;
    }
    return usuario;
}

function exigirCargo(...cargosPermitidos) {
    const usuario = exigirLogin();
    if (!usuario) {
        return null;
    }
    if (!cargosPermitidos.includes(usuario.cargo)) {
        window.alert("Você não tem permissão para acessar esta página.");
        window.location.replace(paginaInicialDoUsuario(usuario));
        return null;
    }
    return usuario;
}

async function fazerLogin(cpf, senha) {
    const resposta = await apiPost("/auth/login", { cpf, senha });
    if (!resposta?.success) {
        throw new Error(resposta?.message || "Não foi possível realizar o login.");
    }

    const usuario = {
        id: resposta.id,
        nome: resposta.nome,
        cargo: resposta.cargo
    };
    salvarSessao(usuario);
    return usuario;
}

function configurarCabecalho() {
    const usuario = getUsuarioLogado();
    const nome = document.querySelector("[data-usuario-nome]");
    if (nome && usuario) {
        nome.textContent = `${usuario.nome} (${usuario.cargo})`;
    }

    document.querySelectorAll("[data-logout]").forEach((botao) => {
        botao.addEventListener("click", (event) => {
            event.preventDefault();
            logout();
        });
    });

    document.querySelectorAll("[data-apenas-dono]").forEach((elemento) => {
        if (usuario?.cargo !== "DONO") {
            elemento.classList.add("hidden");
        }
    });
}
