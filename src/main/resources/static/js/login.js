document.addEventListener("DOMContentLoaded", () => {
    const usuario = getUsuarioLogado();
    if (usuario) {
        window.location.replace(paginaInicialDoUsuario(usuario));
        return;
    }

    const formulario = document.querySelector("#form-login");
    const campoCpf = document.querySelector("#cpf");
    const campoSenha = document.querySelector("#senha");
    const botao = document.querySelector("#btn-entrar");
    const mensagem = document.querySelector("#mensagem");

    campoCpf.addEventListener("input", () => {
        const numeros = campoCpf.value.replace(/\D/g, "").slice(0, 11);
        campoCpf.value = numeros
            .replace(/(\d{3})(\d)/, "$1.$2")
            .replace(/(\d{3})(\d)/, "$1.$2")
            .replace(/(\d{3})(\d{1,2})$/, "$1-$2");
    });

    formulario.addEventListener("submit", async (event) => {
        event.preventDefault();
        const cpf = campoCpf.value.replace(/\D/g, "");
        const senha = campoSenha.value;

        if (cpf.length !== 11 || !senha) {
            exibirMensagem("Informe um CPF com 11 números e a senha.", "error");
            return;
        }

        botao.disabled = true;
        botao.textContent = "Entrando...";
        esconderMensagem();
        try {
            const usuarioLogado = await fazerLogin(cpf, senha);
            window.location.assign(paginaInicialDoUsuario(usuarioLogado));
        } catch (error) {
            exibirMensagem(mensagemErro(error), "error");
        } finally {
            botao.disabled = false;
            botao.textContent = "Entrar";
        }
    });

    function exibirMensagem(texto, tipo) {
        mensagem.textContent = texto;
        mensagem.className = `notice ${tipo}`;
    }

    function esconderMensagem() {
        mensagem.textContent = "";
        mensagem.className = "notice hidden";
    }
});
