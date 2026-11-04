const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');
const code = fs.readFileSync(path.join(__dirname, '../../main/resources/static/assets/checkout.js'), 'utf8');

// ---- DOM mínimo: só o que o checkout.js usa ----

class Elemento {
    constructor(props = {}) {
        Object.assign(this, { value: '', readOnly: false, disabled: false, hidden: false, checked: false,
            textContent: '', src: '', alt: '' }, props);
        this.listeners = {};
    }
    addEventListener(tipo, handler) { (this.listeners[tipo] ||= []).push(handler); }
    async disparar(tipo) { for (const handler of this.listeners[tipo] || []) await handler({ target: this }); }
}

class Formulario extends Elemento {
    constructor(campos, seletores = {}) {
        super();
        this.campos = campos;
        this.seletores = seletores;
        this.elements = { namedItem: nome => this.campos[nome] || null };
    }
    querySelector(seletor) {
        if (seletor === 'input[name="tipoPagamento"]:checked') {
            return this.seletores.radios.find(radio => radio.checked) || null;
        }
        return this.seletores[seletor] || null;
    }
    querySelectorAll(seletor) {
        return seletor === 'input[name="tipoPagamento"]' ? this.seletores.radios : [];
    }
}

function carregar() {
    const window = {};
    const document = { readyState: 'loading', addEventListener() {}, querySelector: () => null, querySelectorAll: () => [] };
    vm.runInNewContext(code, { window, document, encodeURIComponent });
    return window.checkout;
}

function formularioDeEndereco() {
    const campos = Object.fromEntries(['cep', 'logradouro', 'bairro', 'cidade', 'estadoNome', 'estadoSigla', 'numero']
        .map(nome => [nome, new Elemento({ name: nome })]));
    const botao = new Elemento();
    const mensagem = new Elemento({ hidden: true });
    return { form: new Formulario(campos, { 'button[type="submit"]': botao, '[data-cep-mensagem]': mensagem }),
        campos, botao, mensagem };
}

const PRACA_DA_SE = { cep: '01001000', logradouro: 'Praça da Sé', bairro: 'Sé', cidade: 'São Paulo',
    estado_nome: 'São Paulo', estado_sigla: 'SP', geral: false };

function respostaOk(corpo) { return { ok: true, status: 200, json: async () => corpo }; }

// ---- CEP ----

test('ao ligar o formulario os campos do CEP ficam bloqueados e o envio desabilitado', () => {
    const { form, campos, botao } = formularioDeEndereco();

    carregar().ligarCep(form, async () => respostaOk(PRACA_DA_SE));

    for (const nome of ['logradouro', 'bairro', 'cidade', 'estadoNome', 'estadoSigla']) {
        assert.equal(campos[nome].readOnly, true, nome);
    }
    assert.equal(campos.numero.readOnly, false);
    assert.equal(botao.disabled, true);
});

test('CEP completo consulta a loja, preenche os campos bloqueados e libera o envio', async () => {
    const { form, campos, botao } = formularioDeEndereco();
    const consultas = [];
    carregar().ligarCep(form, async url => { consultas.push(url); return respostaOk(PRACA_DA_SE); });

    campos.cep.value = '01001-000';
    await campos.cep.disparar('input');

    assert.deepEqual(consultas, ['/web/conta/cep/01001000']);
    assert.equal(campos.cep.value, '01001000');
    assert.equal(campos.logradouro.value, 'Praça da Sé');
    assert.equal(campos.bairro.value, 'Sé');
    assert.equal(campos.cidade.value, 'São Paulo');
    assert.equal(campos.estadoNome.value, 'São Paulo');
    assert.equal(campos.estadoSigla.value, 'SP');
    assert.equal(campos.logradouro.readOnly, true);
    assert.equal(campos.cidade.readOnly, true);
    assert.equal(botao.disabled, false);
});

test('CEP incompleto nao consulta a loja', async () => {
    const { form, campos, botao } = formularioDeEndereco();
    const consultas = [];
    carregar().ligarCep(form, async url => { consultas.push(url); return respostaOk(PRACA_DA_SE); });

    campos.cep.value = '0100';
    await campos.cep.disparar('input');

    assert.equal(consultas.length, 0);
    assert.equal(botao.disabled, true);
});

test('CEP geral libera rua e bairro para o cliente e bloqueia cidade e estado', async () => {
    const { form, campos, botao } = formularioDeEndereco();
    carregar().ligarCep(form, async () => respostaOk({ cep: '13600000', logradouro: '', bairro: '',
        cidade: 'Araras', estado_nome: 'São Paulo', estado_sigla: 'SP', geral: true }));

    campos.cep.value = '13600000';
    await campos.cep.disparar('input');

    assert.equal(campos.logradouro.readOnly, false);
    assert.equal(campos.bairro.readOnly, false);
    assert.equal(campos.cidade.value, 'Araras');
    assert.equal(campos.cidade.readOnly, true);
    assert.equal(botao.disabled, false);
});

test('CEP inexistente mostra a mensagem da loja, limpa os campos e mantem o envio desabilitado', async () => {
    const { form, campos, botao, mensagem } = formularioDeEndereco();
    carregar().ligarCep(form, async () => ({ ok: false, status: 400,
        json: async () => ({ message: 'CEP não encontrado. Confira os números informados.' }) }));
    campos.logradouro.value = 'Sobra de outra consulta';

    campos.cep.value = '99999999';
    await campos.cep.disparar('input');

    assert.equal(mensagem.hidden, false);
    assert.match(mensagem.textContent, /CEP não encontrado/);
    assert.equal(campos.logradouro.value, '');
    assert.equal(botao.disabled, true);
});

test('falha de rede na consulta mostra mensagem generica', async () => {
    const { form, campos, botao, mensagem } = formularioDeEndereco();
    carregar().ligarCep(form, async () => { throw new Error('rede'); });

    campos.cep.value = '01001000';
    await campos.cep.disparar('input');

    assert.match(mensagem.textContent, /Não foi possível consultar o CEP/);
    assert.equal(botao.disabled, true);
});

test('resposta antiga nao sobrescreve a do CEP digitado por ultimo', async () => {
    const { form, campos } = formularioDeEndereco();
    let liberarPrimeira;
    const primeira = new Promise(resolve => { liberarPrimeira = resolve; });
    carregar().ligarCep(form, async url => url.endsWith('11111111')
        ? primeira : respostaOk(PRACA_DA_SE));

    campos.cep.value = '11111111';
    const consultaLenta = campos.cep.disparar('input');
    campos.cep.value = '01001000';
    await campos.cep.disparar('input');
    liberarPrimeira(respostaOk({ ...PRACA_DA_SE, logradouro: 'Rua Antiga' }));
    await consultaLenta;

    assert.equal(campos.logradouro.value, 'Praça da Sé');
});

// ---- Cartão só com pagamento por cartão ----

function formularioDePagamento() {
    const radios = ['PIX', 'BOLETO', 'CREDIT_CARD'].map(valor => new Elemento({ value: valor, checked: valor === 'PIX' }));
    const cartao = new Elemento();
    const numero = new Elemento({ name: 'numero' });
    const bandeira = new Elemento({ hidden: true });
    const form = new Formulario({}, { radios, '[data-cartao]': cartao, '[data-cartao-numero]': numero,
        '[data-bandeira-novo]': bandeira });
    const escolher = async valor => {
        radios.forEach(radio => { radio.checked = radio.value === valor; });
        await form.disparar('change');
    };
    return { form, cartao, numero, bandeira, escolher };
}

test('com PIX marcado o bloco de cartao fica oculto e desabilitado', () => {
    const { form, cartao } = formularioDePagamento();

    carregar().ligarPagamento(form);

    assert.equal(cartao.hidden, true);
    assert.equal(cartao.disabled, true);
});

test('escolher cartao de credito mostra e habilita o bloco de cartao', async () => {
    const { form, cartao, escolher } = formularioDePagamento();
    carregar().ligarPagamento(form);

    await escolher('CREDIT_CARD');

    assert.equal(cartao.hidden, false);
    assert.equal(cartao.disabled, false);
});

test('voltar para boleto oculta e desabilita o bloco de cartao de novo', async () => {
    const { form, cartao, escolher } = formularioDePagamento();
    carregar().ligarPagamento(form);
    await escolher('CREDIT_CARD');

    await escolher('BOLETO');

    assert.equal(cartao.hidden, true);
    assert.equal(cartao.disabled, true);
});

test('digitar o numero do cartao novo mostra a imagem da bandeira', async () => {
    const { form, numero, bandeira, escolher } = formularioDePagamento();
    carregar().ligarPagamento(form);
    await escolher('CREDIT_CARD');

    numero.value = '4444 4444';
    await numero.disparar('input');

    assert.equal(bandeira.hidden, false);
    assert.equal(bandeira.src, '/assets/bandeiras/visa.svg');
    assert.equal(bandeira.alt, 'Visa');
});

test('numero sem bandeira reconhecida esconde a imagem', async () => {
    const { form, numero, bandeira } = formularioDePagamento();
    carregar().ligarPagamento(form);

    numero.value = '';
    await numero.disparar('input');

    assert.equal(bandeira.hidden, true);
});

// ---- Bandeira pelo início do número ----

test('identifica a bandeira pelos primeiros digitos', () => {
    const { bandeiraDoNumero } = carregar();
    const casos = {
        '4444 4444 4444 4444': 'visa',
        '5184019740373151': 'mastercard',
        '2221000000000009': 'mastercard',
        '2720990000000007': 'mastercard',
        '378282246310005': 'amex',
        '340000000000009': 'amex',
        '6062825624254001': 'hipercard',
        '4011780000000000': 'elo',
        '5067000000000000': 'elo',
        '6363680000000000': 'elo',
        '6011000000000004': 'desconhecida',
        '': 'desconhecida'
    };
    for (const [numero, esperada] of Object.entries(casos)) {
        assert.equal(bandeiraDoNumero(numero), esperada, numero);
    }
});

test('nome da bandeira salva do Asaas vira a imagem correspondente', () => {
    const { imagemDaBandeira } = carregar();
    assert.equal(imagemDaBandeira('MASTERCARD'), '/assets/bandeiras/mastercard.svg');
    assert.equal(imagemDaBandeira('Visa'), '/assets/bandeiras/visa.svg');
    assert.equal(imagemDaBandeira('DISCOVER'), '/assets/bandeiras/desconhecida.svg');
    assert.equal(imagemDaBandeira(null), '/assets/bandeiras/desconhecida.svg');
});
