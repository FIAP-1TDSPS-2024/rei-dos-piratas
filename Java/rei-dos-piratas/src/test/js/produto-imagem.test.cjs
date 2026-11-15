const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');
const code = fs.readFileSync(path.join(__dirname, '../../main/resources/static/assets/produto-imagem.js'), 'utf8');

// ---- DOM mínimo: só o que o produto-imagem.js usa ----

class Elemento {
    constructor(props = {}) {
        Object.assign(this, { value: '', hidden: false, disabled: false, textContent: '', src: '', className: '' }, props);
        this.listeners = {};
    }
    addEventListener(tipo, handler) { (this.listeners[tipo] ||= []).push(handler); }
    async disparar(tipo, evento = {}) {
        const e = { target: this, defaultPrevented: false, propagacaoParada: false,
            preventDefault() { this.defaultPrevented = true; }, stopPropagation() { this.propagacaoParada = true; }, ...evento };
        for (const handler of this.listeners[tipo] || []) await handler(e);
        return e;
    }
}

const CHAVE = 'produtos/3f2b9c1e-8d4a-4b6f-9e2a-1c5d7e9f0a1b/imagem.png';
const URL_UPLOAD = 'https://bucket.s3.amazonaws.com/' + CHAVE + '?X-Amz-Signature=abc';

function montar({ chaveAtual = '' } = {}) {
    const arquivo = new Elemento({ files: [] });
    const chave = new Elemento({ value: chaveAtual });
    const previa = new Elemento({ hidden: !chaveAtual, src: chaveAtual ? 'https://atual' : '' });
    const vazia = new Elemento({ hidden: !!chaveAtual });
    const mensagem = new Elemento({ hidden: true });
    const botao = new Elemento();
    const form = new Elemento();
    form.querySelectorAll = seletor => (seletor === 'button[type="submit"]' ? [botao] : []);
    const seletores = { '[data-imagem-arquivo]': arquivo, '[data-imagem-chave]': chave, '[data-imagem-previa]': previa,
        '[data-imagem-vazia]': vazia, '[data-imagem-mensagem]': mensagem };
    const container = { querySelector: s => seletores[s] || null, closest: () => form };
    return { container, form, arquivo, chave, previa, vazia, mensagem, botao };
}

function carregar() {
    const window = {};
    const document = { readyState: 'loading', addEventListener() {}, querySelectorAll: () => [] };
    vm.runInNewContext(code, { window, document, JSON });
    return window.produtoImagem;
}

const png = (tamanho = 1024) => ({ name: 'capa.png', type: 'image/png', size: tamanho });
const respostaJson = (status, corpo) => ({ ok: status < 400, status, json: async () => corpo });

function dependencias(sobrescritas = {}) {
    const chamadas = { pedirUrl: [], enviar: [] };
    const deps = {
        pedirUrl: async contentType => { chamadas.pedirUrl.push(contentType); return respostaJson(200, { key: CHAVE, url: URL_UPLOAD }); },
        enviar: async (url, arquivo) => { chamadas.enviar.push({ url, arquivo }); return { ok: true, status: 200 }; },
        criarPrevia: () => 'blob:previa-local',
        ...sobrescritas
    };
    return { deps, chamadas };
}

async function escolher(tela, arquivo) {
    tela.arquivo.files = [arquivo];
    await tela.arquivo.disparar('change');
}

// ---- Validação antes de enviar ----

test('aceita PNG, JPEG e WEBP de ate 5 MB', () => {
    const { validarArquivo } = carregar();
    assert.equal(validarArquivo({ type: 'image/png', size: 10 }), null);
    assert.equal(validarArquivo({ type: 'image/jpeg', size: 5 * 1024 * 1024 }), null);
    assert.equal(validarArquivo({ type: 'image/webp', size: 10 }), null);
});

test('recusa outro formato ou arquivo maior que 5 MB com mensagem', () => {
    const { validarArquivo } = carregar();
    assert.match(validarArquivo({ type: 'image/gif', size: 10 }), /PNG, JPEG ou WEBP/);
    assert.match(validarArquivo({ type: 'application/pdf', size: 10 }), /PNG, JPEG ou WEBP/);
    assert.match(validarArquivo({ type: 'image/png', size: 5 * 1024 * 1024 + 1 }), /5 MB/);
});

// ---- Envio ----

test('escolher a imagem pede a URL, envia ao S3 com o tipo do arquivo e guarda so a chave', async () => {
    const tela = montar();
    const { deps, chamadas } = dependencias();
    carregar().ligar(tela.container, deps);

    await escolher(tela, png());

    assert.deepEqual(chamadas.pedirUrl, ['image/png']);
    assert.equal(chamadas.enviar.length, 1);
    assert.equal(chamadas.enviar[0].url, URL_UPLOAD);
    assert.equal(tela.chave.value, CHAVE);
    assert.equal(tela.previa.src, 'blob:previa-local');
    assert.equal(tela.previa.hidden, false);
    assert.equal(tela.vazia.hidden, true);
    assert.equal(tela.botao.disabled, false);
    assert.match(tela.mensagem.textContent, /enviada/);
});

test('o botao de salvar fica desabilitado enquanto a imagem sobe', async () => {
    const tela = montar();
    let liberar;
    const { deps } = dependencias({ enviar: () => new Promise(resolve => { liberar = resolve; }) });
    carregar().ligar(tela.container, deps);

    tela.arquivo.files = [png()];
    const envio = tela.arquivo.disparar('change');
    await new Promise(resolve => setImmediate(resolve));

    assert.equal(tela.botao.disabled, true);
    liberar({ ok: true, status: 200 });
    await envio;
    assert.equal(tela.botao.disabled, false);
});

test('formato invalido nao chama a loja nem o S3 e mantem a imagem anterior', async () => {
    const tela = montar({ chaveAtual: CHAVE });
    const { deps, chamadas } = dependencias();
    carregar().ligar(tela.container, deps);

    await escolher(tela, { name: 'a.gif', type: 'image/gif', size: 10 });

    assert.equal(chamadas.pedirUrl.length, 0);
    assert.equal(tela.chave.value, CHAVE);
    assert.equal(tela.mensagem.hidden, false);
    assert.match(tela.mensagem.textContent, /PNG, JPEG ou WEBP/);
    assert.equal(tela.arquivo.value, '');
});

test('erro da loja ao gerar a URL mostra a mensagem dela e nao envia', async () => {
    const tela = montar();
    const { deps, chamadas } = dependencias({
        pedirUrl: async () => respostaJson(502, { message: 'Armazenamento de imagens não configurado. Avise o suporte.' })
    });
    carregar().ligar(tela.container, deps);

    await escolher(tela, png());

    assert.equal(chamadas.enviar.length, 0);
    assert.match(tela.mensagem.textContent, /não configurado/);
    assert.equal(tela.chave.value, '');
    assert.equal(tela.botao.disabled, false);
});

test('sessao expirada ou sem permissao ao gerar a URL mostra mensagem generica', async () => {
    const tela = montar();
    const { deps } = dependencias({ pedirUrl: async () => ({ ok: false, status: 403, json: async () => { throw new Error('html'); } }) });
    carregar().ligar(tela.container, deps);

    await escolher(tela, png());

    assert.match(tela.mensagem.textContent, /Não foi possível preparar o envio/);
});

test('S3 recusando o envio (URL expirada ou assinatura) pede para escolher de novo', async () => {
    const tela = montar();
    const { deps } = dependencias({ enviar: async () => ({ ok: false, status: 403 }) });
    carregar().ligar(tela.container, deps);

    await escolher(tela, png());

    assert.match(tela.mensagem.textContent, /recusou/);
    assert.equal(tela.chave.value, '');
    assert.equal(tela.botao.disabled, false);
});

test('falha de rede ou CORS no envio ao S3 mostra mensagem de conexao', async () => {
    const tela = montar();
    const { deps } = dependencias({ enviar: async () => { throw new TypeError('Failed to fetch'); } });
    carregar().ligar(tela.container, deps);

    await escolher(tela, png());

    assert.match(tela.mensagem.textContent, /conexão/);
    assert.equal(tela.chave.value, '');
});

test('URL de upload que nao e HTTPS nao e usada', async () => {
    const tela = montar();
    const { deps, chamadas } = dependencias({ pedirUrl: async () => respostaJson(200, { key: CHAVE, url: 'http://inseguro/upload' }) });
    carregar().ligar(tela.container, deps);

    await escolher(tela, png());

    assert.equal(chamadas.enviar.length, 0);
    assert.equal(tela.chave.value, '');
});

test('resultado de um envio antigo nao sobrescreve a imagem escolhida depois', async () => {
    const tela = montar();
    let liberarPrimeiro;
    let envios = 0;
    const { deps } = dependencias({
        pedirUrl: async () => { envios++; return respostaJson(200, { key: envios === 1 ? 'produtos/antiga' : CHAVE, url: URL_UPLOAD }); },
        enviar: () => (envios === 1 ? new Promise(resolve => { liberarPrimeiro = resolve; }) : Promise.resolve({ ok: true, status: 200 }))
    });
    carregar().ligar(tela.container, deps);

    tela.arquivo.files = [png()];
    const lento = tela.arquivo.disparar('change');
    await new Promise(resolve => setImmediate(resolve));
    await escolher(tela, png());
    liberarPrimeiro({ ok: true, status: 200 });
    await lento;

    assert.equal(tela.chave.value, CHAVE);
});

// ---- Salvar ----

test('salvar sem imagem e bloqueado antes de chegar ao envio autenticado', async () => {
    const tela = montar();
    carregar().ligar(tela.container, dependencias().deps);

    const evento = await tela.form.disparar('submit');

    assert.equal(evento.defaultPrevented, true);
    assert.equal(evento.propagacaoParada, true);
    assert.match(tela.mensagem.textContent, /Selecione a imagem/);
});

test('salvar com a imagem ja enviada segue normalmente', async () => {
    const tela = montar({ chaveAtual: CHAVE });
    carregar().ligar(tela.container, dependencias().deps);

    const evento = await tela.form.disparar('submit');

    assert.equal(evento.defaultPrevented, false);
    assert.equal(evento.propagacaoParada, false);
});
