// Imagem do produto no formulário web: o navegador pede à loja uma URL pré-assinada, envia o arquivo
// direto ao S3 (PUT com o Content-Type assinado) e guarda no formulário só a chave do objeto.
// Nunca registra a URL assinada (ela dá acesso ao objeto enquanto vale).
(() => {
    const TIPOS = ['image/png', 'image/jpeg', 'image/webp'];
    const TAMANHO_MAXIMO = 5 * 1024 * 1024;
    const MENSAGENS = {
        formato: 'Formato não suportado. Use PNG, JPEG ou WEBP.',
        tamanho: 'A imagem deve ter até 5 MB.',
        preparar: 'Não foi possível preparar o envio da imagem. Recarregue a página e tente novamente.',
        recusada: 'O armazenamento recusou a imagem (o envio pode ter expirado). Selecione a imagem novamente.',
        conexao: 'Não foi possível enviar a imagem. Verifique a conexão e tente novamente.',
        enviando: 'Enviando imagem...',
        enviada: 'Imagem enviada. Salve o produto para concluir.',
        obrigatoria: 'Selecione a imagem do produto antes de salvar.'
    };

    function validarArquivo(arquivo) {
        if (!arquivo || !TIPOS.includes(arquivo.type)) return MENSAGENS.formato;
        if (arquivo.size > TAMANHO_MAXIMO) return MENSAGENS.tamanho;
        return null;
    }

    function pedirUrlPadrao(contentType) {
        return window.webAuth.fetch('/web/produtos/imagem/upload-url', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
            body: JSON.stringify({ content_type: contentType })
        });
    }

    function enviarPadrao(url, arquivo) {
        // Sem credenciais da loja: a autorização está na própria URL assinada
        return window.fetch(url, { method: 'PUT', headers: { 'Content-Type': arquivo.type }, body: arquivo, credentials: 'omit' });
    }

    function ligar(container, deps = {}) {
        const pedirUrl = deps.pedirUrl || pedirUrlPadrao;
        const enviar = deps.enviar || enviarPadrao;
        const criarPrevia = deps.criarPrevia || (arquivo => window.URL.createObjectURL(arquivo));

        const arquivo = container.querySelector('[data-imagem-arquivo]');
        const chave = container.querySelector('[data-imagem-chave]');
        const previa = container.querySelector('[data-imagem-previa]');
        const vazia = container.querySelector('[data-imagem-vazia]');
        const mensagem = container.querySelector('[data-imagem-mensagem]');
        const form = container.closest('form');
        const botoes = () => (form ? Array.from(form.querySelectorAll('button[type="submit"]')) : []);
        let envioAtual = 0;

        const avisar = (texto, erro = false) => {
            mensagem.textContent = texto;
            mensagem.hidden = !texto;
            mensagem.className = erro ? 'text-sm text-red-600' : 'text-sm text-gray-600';
        };
        const bloquearSalvar = bloqueado => botoes().forEach(botao => { botao.disabled = bloqueado; });
        const mensagemDaLoja = async resposta => {
            try {
                const corpo = await resposta.json();
                return (corpo && typeof corpo.message === 'string') ? corpo.message : MENSAGENS.preparar;
            } catch (e) {
                return MENSAGENS.preparar;
            }
        };

        arquivo.addEventListener('change', async () => {
            const escolhido = arquivo.files && arquivo.files[0];
            if (!escolhido) return;
            const erro = validarArquivo(escolhido);
            if (erro) {
                arquivo.value = '';
                avisar(erro, true);
                return;
            }

            const envio = ++envioAtual;
            const atual = () => envio === envioAtual;
            bloquearSalvar(true);
            avisar(MENSAGENS.enviando);
            try {
                const resposta = await pedirUrl(escolhido.type);
                if (!atual()) return;
                if (!resposta.ok) {
                    avisar(await mensagemDaLoja(resposta), true);
                    return;
                }
                const destino = await resposta.json();
                if (!atual()) return;
                if (!destino || typeof destino.key !== 'string' || typeof destino.url !== 'string'
                        || !destino.url.startsWith('https://')) {
                    avisar(MENSAGENS.preparar, true);
                    return;
                }

                let envioS3;
                try {
                    envioS3 = await enviar(destino.url, escolhido);
                } catch (e) {
                    if (atual()) avisar(MENSAGENS.conexao, true);
                    return;
                }
                if (!atual()) return;
                if (!envioS3.ok) {
                    avisar(MENSAGENS.recusada, true);
                    return;
                }

                chave.value = destino.key;
                previa.src = criarPrevia(escolhido);
                previa.hidden = false;
                if (vazia) vazia.hidden = true;
                avisar(MENSAGENS.enviada);
            } catch (e) {
                if (atual()) avisar(MENSAGENS.preparar, true);
            } finally {
                if (atual()) bloquearSalvar(false);
            }
        });

        if (form) {
            form.addEventListener('submit', event => {
                if (chave.value) return;
                // Para aqui: o web-auth.js (no document) enviaria o formulário mesmo com preventDefault
                event.preventDefault();
                event.stopPropagation();
                avisar(MENSAGENS.obrigatoria, true);
            });
        }
    }

    window.produtoImagem = { validarArquivo, ligar };

    const iniciar = () => document.querySelectorAll('[data-imagem-produto]').forEach(container => ligar(container));
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', iniciar);
    else iniciar();
})();
