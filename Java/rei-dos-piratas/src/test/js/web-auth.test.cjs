const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');
const code = fs.readFileSync(path.join(__dirname, '../../main/resources/static/assets/web-auth.js'), 'utf8');

function environment({ expired = true, locks = true, refreshFailure = false, statuses = [200] } = {}) {
    let expires = Date.now() + (expired ? -1000 : 900000);
    const calls = [];
    const queues = new Map();
    const lockManager = { request(name, callback) {
        const next = (queues.get(name) || Promise.resolve()).catch(() => {}).then(callback);
        queues.set(name, next);
        return next;
    } };
    async function fetch(url, options = {}) {
        calls.push({ url, options });
        if (url === '/web/auth/csrf') {
            return { ok: true, json: async () => ({ token: 'masked-csrf', headerName: 'X-XSRF-TOKEN', parameterName: '_csrf' }) };
        }
        if (url === '/web/auth/refresh') {
            await new Promise(resolve => setTimeout(resolve, 15));
            if (refreshFailure) throw new Error('network');
            expires = Date.now() + 900000;
            return { ok: true, status: 204 };
        }
        if (url === '/web/auth/login') return { ok: true, status: 204 };
        const status = statuses.shift() || 200;
        return { ok: status < 400, status };
    }
    function tab() {
        const listeners = {};
        const redirects = [];
        class Form {
            constructor() {
                this.method = 'post'; this.action = 'https://shop.example/web/pedidos/imprimir';
                this.target = '_blank'; this.dataset = {}; this.inputs = []; this.submissions = [];
                this.elements = { namedItem: name => this.inputs.find(input => input.name === name) };
            }
            appendChild(input) { this.inputs.push(input); input.remove = () => this.inputs.splice(this.inputs.indexOf(input), 1); }
            submit() { this.submissions.push(this.inputs.map(input => ({ name: input.name, value: input.value }))); }
        }
        const location = { origin: 'https://shop.example', pathname: '/web/produtos',
            assign(url) { redirects.push(url); }, reload() {} };
        const document = {
            get cookie() { return `auth_expires_at=${expires}`; },
            set cookie(value) { throw new Error('Tokens nao devem ser gravados por JS'); },
            visibilityState: 'hidden', addEventListener(name, handler) { listeners[name] = handler; },
            querySelector() { return {}; }, createElement() { return {}; }
        };
        const window = { fetch, location };
        vm.runInNewContext(code, { window, document, location, navigator: locks ? { locks: lockManager } : {},
            Date, URL, Headers, setTimeout, clearTimeout, HTMLFormElement: Form });
        window.webAuth.testUi = { Form, listeners, redirects };
        return window.webAuth;
    }
    return { tab, calls };
}

test('duas abas e chamadas paralelas consomem somente um refresh', async () => {
    const env = environment();
    const a = env.tab();
    const b = env.tab();
    await Promise.all([a.renew(), a.renew(), b.renew()]);
    const refresh = env.calls.filter(call => call.url === '/web/auth/refresh');
    assert.equal(refresh.length, 1);
    assert.equal(refresh[0].options.headers['X-XSRF-TOKEN'], 'masked-csrf');
    assert.equal(refresh[0].options.credentials, 'same-origin');
    assert.equal(refresh[0].options.body, undefined);
});

test('login usa CSRF e cookies do servidor sem gravar tokens via JavaScript', async () => {
    const env = environment({ expired: false });
    await env.tab().login('user@example.com', 'senha');
    const login = env.calls.find(call => call.url === '/web/auth/login');
    assert.equal(login.options.headers['X-XSRF-TOKEN'], 'masked-csrf');
    assert.deepEqual(JSON.parse(login.options.body), { email: 'user@example.com', password: 'senha' });
});

test('falha de rede nao repete automaticamente refresh ja possivelmente consumido', async () => {
    const env = environment({ refreshFailure: true });
    await assert.rejects(env.tab().renew(), /network/);
    assert.equal(env.calls.filter(call => call.url === '/web/auth/refresh').length, 1);
});

test('navegador sem locks pede novo login ao expirar, sem arriscar reutilizacao', async () => {
    const env = environment({ locks: false });
    await assert.rejects(env.tab().renew(), /Entre novamente/);
    assert.equal(env.calls.length, 0);
});

test('401 renova e repete uma vez; 403 nao repete operacoes', async () => {
    const env = environment({ expired: false, statuses: [401, 200] });
    const response = await env.tab().fetch('/web/operacao', { method: 'POST' });
    assert.equal(response.status, 200);
    assert.equal(env.calls.filter(call => call.url === '/web/operacao').length, 2);
    assert.equal(env.calls.filter(call => call.url === '/web/auth/refresh').length, 1);
    const denied = environment({ expired: false, statuses: [403] });
    assert.equal((await denied.tab().fetch('/web/operacao', { method: 'POST' })).status, 403);
    assert.equal(denied.calls.filter(call => call.url === '/web/operacao').length, 1);
});

test('cliente web recusa encaminhar credenciais para outro dominio ou para a API', async () => {
    const env = environment({ expired: false });
    const auth = env.tab();
    await assert.rejects(auth.fetch('https://evil.example/web/operacao'), /Destino/);
    await assert.rejects(auth.fetch('/auth/logout'), /Destino/);
    assert.equal(env.calls.length, 0);
});

test('formulario renova antes do envio nativo com CSRF e preserva download e botao', async () => {
    const env = environment();
    const auth = env.tab();
    const form = new auth.testUi.Form();
    let prevented = 0;
    const event = { target: form, submitter: { name: 'acao', value: 'imprimir' },
        preventDefault() { prevented++; } };
    await auth.testUi.listeners.submit(event);
    await auth.testUi.listeners.submit(event);
    assert.equal(prevented, 2);
    assert.equal(env.calls.filter(call => call.url === '/web/auth/refresh').length, 1);
    assert.equal(form.submissions.length, 2);
    for (const submission of form.submissions) {
        assert.equal(submission.filter(input => input.name === 'acao').length, 1);
        assert.equal(submission.find(input => input.name === '_csrf').value, 'masked-csrf');
    }
    assert.equal(form.target, '_blank');
    assert.equal(form.dataset.authSubmitting, undefined);
});

test('falha de renovacao impede envio do formulario e encaminha para login', async () => {
    const auth = environment({ refreshFailure: true }).tab();
    const form = new auth.testUi.Form();
    await auth.testUi.listeners.submit({ target: form, preventDefault() {} });
    assert.equal(form.submissions.length, 0);
    assert.deepEqual(auth.testUi.redirects, ['/web/login?error']);
});
