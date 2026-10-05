(() => {
    'use strict';
    const request = window.fetch.bind(window);
    let renewal;
    let timer;

    function expiration() {
        const cookie = document.cookie.split('; ').find(value => value.startsWith('auth_expires_at='));
        return cookie ? Number(cookie.substring('auth_expires_at='.length)) : 0;
    }

    async function csrf() {
        const operation = async () => {
            const response = await request('/web/auth/csrf', { credentials: 'same-origin', cache: 'no-store' });
            if (!response.ok) throw new Error('Não foi possível concluir a operação. Tente novamente.');
            return response.json();
        };
        return navigator.locks ? navigator.locks.request('rei-dos-piratas-csrf', operation) : operation();
    }

    async function renew(force = false) {
        if (renewal) return renewal;
        const observed = expiration();
        if (!force && (!observed || observed > Date.now() + 60000)) return false;
        // Sem exclusao entre abas, solicitamos novo login em vez de arriscar reutilizar o refresh.
        if (!navigator.locks) throw new Error('Entre novamente para continuar.');
        renewal = navigator.locks.request('rei-dos-piratas-auth', async () => {
            const current = expiration();
            if (current > Date.now() + 60000 && (!force || current !== observed)) return false;
            const token = await csrf();
            const response = await request('/web/auth/refresh', {
                method: 'POST', credentials: 'same-origin', cache: 'no-store',
                headers: { [token.headerName]: token.token }
            });
            // Nao repetir refresh em falha de rede: a primeira chamada pode ter sido confirmada.
            if (!response.ok) throw new Error('Entre novamente para continuar.');
            schedule();
            return true;
        });
        try { return await renewal; }
        finally { renewal = undefined; }
    }

    function schedule() {
        clearTimeout(timer);
        const expires = expiration();
        if (!expires || document.visibilityState === 'hidden') return;
        timer = setTimeout(() => renew().then(schedule).catch(goToLogin), Math.max(1000, expires - Date.now() - 60000));
    }

    function goToLogin() { window.location.assign('/web/login?error'); }

    async function login(email, password) {
        const operation = async () => {
            const token = await csrf();
            const response = await request('/web/auth/login', {
                method: 'POST', credentials: 'same-origin', cache: 'no-store',
                headers: { 'Content-Type': 'application/json', [token.headerName]: token.token },
                body: JSON.stringify({ email, password })
            });
            if (!response.ok) throw new Error('Credenciais inválidas. Verifique seu e-mail e senha.');
        };
        if (navigator.locks) return navigator.locks.request('rei-dos-piratas-auth', operation);
        return operation();
    }

    async function authenticatedFetch(url, options = {}) {
        const target = new URL(url, location.origin);
        if (target.origin !== location.origin || !target.pathname.startsWith('/web/')) {
            throw new Error('Destino inválido.');
        }
        await renew();
        const execute = async () => {
            const headers = new Headers(options.headers);
            const method = (options.method || 'GET').toUpperCase();
            if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
                const token = await csrf();
                headers.set(token.headerName, token.token);
            }
            return request(url, { ...options, headers, credentials: 'same-origin' });
        };
        let response = await execute();
        if (response.status === 401) {
            await renew(true);
            response = await execute();
        }
        return response;
    }

    window.webAuth = { csrf, login, renew, fetch: authenticatedFetch };

    document.addEventListener('submit', async event => {
        const form = event.target;
        if (!(form instanceof HTMLFormElement) || form.id === 'loginForm'
                || form.method.toLowerCase() === 'get') return;
        const target = new URL(form.action, location.origin);
        if (target.origin !== location.origin || !target.pathname.startsWith('/web/')) return;
        event.preventDefault();
        if (form.dataset.authSubmitting) return;
        form.dataset.authSubmitting = 'true';
        const submitter = event.submitter;
        try {
            await renew();
            const token = await csrf();
            let input = form.elements.namedItem(token.parameterName);
            if (!input) {
                input = document.createElement('input');
                input.type = 'hidden';
                input.name = token.parameterName;
                form.appendChild(input);
            }
            input.value = token.token;
            let button;
            // Preserva envio nativo, validacao, arquivos, target e downloads.
            if (submitter && submitter.name) {
                button = document.createElement('input');
                button.type = 'hidden'; button.name = submitter.name; button.value = submitter.value;
                form.appendChild(button);
            }
            HTMLFormElement.prototype.submit.call(form);
            if (button) button.remove();
            delete form.dataset.authSubmitting;
        } catch (error) {
            delete form.dataset.authSubmitting;
            goToLogin();
        }
    }, true);

    function resume() {
        if (['/web/login', '/web/renovar'].includes(location.pathname)) return;
        const unauthenticatedPage = Boolean(expiration()) && !document.querySelector('meta[name="web-authenticated"]');
        renew(unauthenticatedPage).then(() => {
            if (unauthenticatedPage && expiration() > Date.now()) location.reload();
            else schedule();
        }).catch(goToLogin);
    }
    document.addEventListener('DOMContentLoaded', resume);
    document.addEventListener('visibilitychange', () => {
        if (document.visibilityState === 'visible') resume();
        else clearTimeout(timer);
    });
})();
