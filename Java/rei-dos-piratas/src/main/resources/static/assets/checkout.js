// Checkout da área do cliente: endereço pelo CEP (campos oficiais bloqueados), bloco de cartão só com
// pagamento por cartão e imagem da bandeira. Não lê nem guarda dados do cartão além do necessário para
// mostrar a bandeira (nada vai para console, storage ou rede).
(() => {
    const CAMPOS_DO_CEP = ['logradouro', 'bairro', 'cidade', 'estadoNome', 'estadoSigla'];
    const RUA_E_BAIRRO = ['logradouro', 'bairro'];
    const BANDEIRAS = { visa: 'Visa', mastercard: 'Mastercard', amex: 'American Express', elo: 'Elo', hipercard: 'Hipercard' };
    const FAIXAS_ELO = [[401178, 401179], [431274, 431274], [438935, 438935], [451416, 451416], [457393, 457393],
        [457631, 457632], [504175, 504175], [506699, 506778], [509000, 509999], [627780, 627780], [636297, 636297],
        [636368, 636368], [650031, 650033], [650035, 650051], [650405, 650439], [650485, 650538], [650541, 650598],
        [650700, 650718], [650720, 650727], [650901, 650920], [651652, 651679], [655000, 655019], [655021, 655058]];

    function bandeiraDoNumero(numero) {
        const digitos = String(numero || '').replace(/\D/g, '');
        const inicio6 = Number(digitos.slice(0, 6));
        if (digitos.length >= 6 && FAIXAS_ELO.some(([de, ate]) => inicio6 >= de && inicio6 <= ate)) return 'elo';
        if (/^(606282|3841)/.test(digitos)) return 'hipercard';
        if (/^3[47]/.test(digitos)) return 'amex';
        const inicio4 = Number(digitos.slice(0, 4));
        if (/^5[1-5]/.test(digitos) || (digitos.length >= 4 && inicio4 >= 2221 && inicio4 <= 2720)) return 'mastercard';
        if (/^4/.test(digitos)) return 'visa';
        return 'desconhecida';
    }

    function imagemDaBandeira(nome) {
        const chave = String(nome || '').toLowerCase();
        return `/assets/bandeiras/${BANDEIRAS[chave] ? chave : 'desconhecida'}.svg`;
    }

    function buscarPadrao(url) {
        const cliente = window.webAuth && window.webAuth.fetch ? window.webAuth.fetch : window.fetch;
        return cliente(url, { headers: { Accept: 'application/json' } });
    }

    function ligarCep(form, buscar = buscarPadrao) {
        const campo = nome => form.elements.namedItem(nome);
        const botao = form.querySelector('button[type="submit"]');
        const mensagem = form.querySelector('[data-cep-mensagem]');
        let ultimaConsulta = 0;

        const bloquear = (nomes, bloqueado) => nomes.forEach(nome => { if (campo(nome)) campo(nome).readOnly = bloqueado; });
        const limpar = () => CAMPOS_DO_CEP.forEach(nome => { if (campo(nome)) campo(nome).value = ''; });
        const avisar = texto => {
            if (!mensagem) return;
            mensagem.textContent = texto || '';
            mensagem.hidden = !texto;
        };
        const liberarEnvio = liberado => { if (botao) botao.disabled = !liberado; };

        bloquear(CAMPOS_DO_CEP, true);
        liberarEnvio(false);

        campo('cep').addEventListener('input', async () => {
            const digitos = campo('cep').value.replace(/\D/g, '').slice(0, 8);
            campo('cep').value = digitos;
            const consulta = ++ultimaConsulta;
            liberarEnvio(false);
            bloquear(CAMPOS_DO_CEP, true);
            if (digitos.length !== 8) {
                avisar('');
                return;
            }
            avisar('Consultando o CEP...');
            try {
                const resposta = await buscar(`/web/conta/cep/${encodeURIComponent(digitos)}`);
                const corpo = await resposta.json().catch(() => null);
                if (consulta !== ultimaConsulta) return;
                if (!resposta.ok || !corpo) {
                    limpar();
                    avisar((corpo && corpo.message) || 'CEP não encontrado. Confira os números informados.');
                    return;
                }
                campo('logradouro').value = corpo.logradouro || '';
                campo('bairro').value = corpo.bairro || '';
                campo('cidade').value = corpo.cidade || '';
                campo('estadoNome').value = corpo.estado_nome || '';
                campo('estadoSigla').value = corpo.estado_sigla || '';
                if (corpo.geral) bloquear(RUA_E_BAIRRO, false);
                avisar(corpo.geral ? 'CEP geral da cidade: informe a rua e o bairro.' : '');
                liberarEnvio(true);
            } catch (e) {
                if (consulta !== ultimaConsulta) return;
                limpar();
                avisar('Não foi possível consultar o CEP agora. Tente novamente em instantes.');
            }
        });
    }

    function ligarPagamento(form) {
        const cartao = form.querySelector('[data-cartao]');
        const numero = form.querySelector('[data-cartao-numero]');
        const bandeira = form.querySelector('[data-bandeira-novo]');

        const atualizarCartao = () => {
            const escolhido = form.querySelector('input[name="tipoPagamento"]:checked');
            const comCartao = !!escolhido && escolhido.value === 'CREDIT_CARD';
            if (!cartao) return;
            cartao.hidden = !comCartao;
            // fieldset desabilitado não envia os campos: PIX/boleto não levam dados de cartão
            cartao.disabled = !comCartao;
        };
        const atualizarBandeira = () => {
            if (!numero || !bandeira) return;
            const marca = bandeiraDoNumero(numero.value);
            bandeira.hidden = marca === 'desconhecida';
            bandeira.src = imagemDaBandeira(marca);
            bandeira.alt = BANDEIRAS[marca] || '';
        };

        form.addEventListener('change', atualizarCartao);
        if (numero) numero.addEventListener('input', atualizarBandeira);
        atualizarCartao();
        atualizarBandeira();
    }

    window.checkout = { bandeiraDoNumero, imagemDaBandeira, ligarCep, ligarPagamento };

    const iniciar = () => {
        document.querySelectorAll('form[data-cep-form]').forEach(form => ligarCep(form));
        document.querySelectorAll('form[data-pagamento-form]').forEach(form => ligarPagamento(form));
    };
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', iniciar);
    else iniciar();
})();
