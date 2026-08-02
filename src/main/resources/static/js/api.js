const API_BASE_URL = "/api";

async function apiRequest(path, options = {}) {
    const requestOptions = {
        method: options.method || "GET",
        credentials: "same-origin",
        headers: {
            Accept: "application/json",
            ...(options.body !== undefined ? { "Content-Type": "application/json" } : {}),
            ...(options.headers || {})
        }
    };

    if (options.body !== undefined) {
        requestOptions.body = JSON.stringify(options.body);
    }

    let response;
    try {
        response = await fetch(`${API_BASE_URL}${path}`, requestOptions);
    } catch (error) {
        throw new Error("Não foi possível conectar ao servidor. Verifique se a aplicação Spring Boot está em execução.");
    }

    if (response.status === 204) {
        return null;
    }

    const text = await response.text();
    let payload = null;
    if (text) {
        try {
            payload = JSON.parse(text);
        } catch {
            payload = { message: text };
        }
    }

    if (!response.ok) {
        const message = payload?.message || payload?.error || `A operação falhou (HTTP ${response.status}).`;
        const error = new Error(message);
        error.status = response.status;
        error.payload = payload;
        throw error;
    }

    return payload;
}

function apiGet(path) {
    return apiRequest(path);
}

function apiPost(path, body) {
    return apiRequest(path, { method: "POST", body });
}

function apiPut(path, body) {
    return apiRequest(path, { method: "PUT", body });
}

function apiPatch(path, body) {
    return apiRequest(path, { method: "PATCH", body });
}

function apiDelete(path) {
    return apiRequest(path, { method: "DELETE" });
}

function mensagemErro(error) {
    return error instanceof Error ? error.message : "Ocorreu um erro inesperado.";
}
