const form = document.querySelector("#form-upload");
const keyInput = document.querySelector("#key");
const urlInput = document.querySelector("#url");
const imagemInput = document.querySelector("#imagem");
const enviarButton = document.querySelector("#enviar");
const resultado = document.querySelector("#resultado");

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const key = keyInput.value.trim();
    const urlInformada = urlInput.value.trim();
    const imagem = imagemInput.files[0];

    if (!key || !urlInformada || !imagem) {
        mostrarResultado("Preencha a key, a URL e selecione uma imagem.", "erro");
        return;
    }

    if (!imagem.type.startsWith("image/")) {
        mostrarResultado("Selecione um arquivo de imagem válido.", "erro");
        return;
    }

    let url;
    try {
        url = new URL(urlInformada);
    } catch {
        mostrarResultado("A URL pré-assinada não é válida.", "erro");
        return;
    }

    if (url.protocol !== "https:") {
        mostrarResultado("A URL pré-assinada deve usar HTTPS.", "erro");
        return;
    }

    enviarButton.disabled = true;
    mostrarResultado(`Enviando ${imagem.name} para a key ${key}...`);

    try {
        const resposta = await fetch(url, {
            method: "PUT",
            headers: imagem.type ? { "Content-Type": imagem.type } : {},
            body: imagem
        });

        if (!resposta.ok) {
            mostrarResultado(`O S3 respondeu com HTTP ${resposta.status}. Confira a URL, a key e o CORS do bucket.`, "erro");
            return;
        }

        mostrarResultado(`Upload concluído. Key enviada: ${key}`, "sucesso");
    } catch {
        mostrarResultado("Não foi possível concluir o upload. Verifique a conexão e o CORS do bucket.", "erro");
    } finally {
        enviarButton.disabled = false;
    }
});

function mostrarResultado(mensagem, tipo = "") {
    resultado.textContent = mensagem;
    resultado.className = `resultado ${tipo}`.trim();
}
