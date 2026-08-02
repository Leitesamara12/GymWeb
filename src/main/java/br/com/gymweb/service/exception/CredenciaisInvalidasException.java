package br.com.gymweb.service.exception;

/**
 * Erro único para CPF inexistente ou senha incorreta — evita
 * enumeração de usuários via mensagem diferenciada.
 */
public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}
