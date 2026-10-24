package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.CartaoCliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.PagamentoCartao;

import java.util.List;

public interface CartaoService {
    /** Retorna o cartão salvo informado ou tokeniza e salva o cartão novo. */
    CartaoCliente obterOuTokenizar(Cliente cliente, Endereco endereco, PagamentoCartao pagamentoCartao);
    /** Uso interno: todos os cartões (ativos e inativos) de um cliente. */
    List<CartaoCliente> findAllByClienteId(Long clienteId);
    /** Cartões ativos do cliente logado; é o único acesso exposto pela API. */
    List<CartaoCliente> listarCartoesDoClienteAtual();
}
