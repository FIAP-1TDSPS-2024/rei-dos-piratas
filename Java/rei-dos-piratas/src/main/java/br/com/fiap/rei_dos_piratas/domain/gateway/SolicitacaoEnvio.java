package br.com.fiap.rei_dos_piratas.domain.gateway;

import br.com.fiap.rei_dos_piratas.domain.entity.DadosEmpresa;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.domain.entity.Volume;

import java.math.BigDecimal;
import java.util.List;

/** Envio a criar para um pedido: da loja ao cliente, com o serviço escolhido e seguro pelo valor do pedido. */
public record SolicitacaoEnvio(
        Long servico,
        ParteEnvio remetente,
        ParteEnvio destinatario,
        List<ProdutoEnvio> produtos,
        Volume volume,
        String plataforma,
        String lembrete,
        BigDecimal valorSeguro
) {
    public static final String PLATAFORMA = "Rei dos Piratas";

    public static SolicitacaoEnvio para(Pedido pedido, Endereco enderecoLoja, DadosEmpresa loja) {
        ParteEnvio remetente = new ParteEnvio(
                loja.getNomeFantasia(),
                loja.getEmail(),
                loja.getTelefone(),
                null,
                loja.getCnpj(),
                loja.getStateRegister(),
                loja.getEconomicActivityCode(),
                enderecoLoja.getLogradouro(),
                null,
                String.valueOf(enderecoLoja.getNumero()),
                enderecoLoja.getBairro(),
                enderecoLoja.getCidade().getNome(),
                enderecoLoja.getCep(),
                enderecoLoja.getCidade().getEstado().getSigla());

        Endereco entrega = pedido.getEnderecoEntrega();
        ParteEnvio destinatario = new ParteEnvio(
                pedido.getCliente().getNomeCompleto(),
                pedido.getCliente().getEmail(),
                pedido.getCliente().getCelular(),
                pedido.getCliente().getCpf(),
                null,
                null,
                null,
                entrega.getLogradouro(),
                null,
                String.valueOf(entrega.getNumero()),
                entrega.getBairro(),
                entrega.getCidade().getNome(),
                entrega.getCep(),
                entrega.getCidade().getEstado().getSigla());

        List<ProdutoEnvio> produtos = pedido.getProdutosAdicionados().stream()
                .map(item -> new ProdutoEnvio(item.getProduto().getNome(), item.getQuantidade(), item.getPrecoUnitario()))
                .toList();

        return new SolicitacaoEnvio(
                pedido.getServicoEntrega(),
                remetente,
                destinatario,
                produtos,
                Volume.para(pedido.getProdutosAdicionados()),
                PLATAFORMA,
                "Entrega do pedido " + pedido.getId(),
                pedido.getValorTotal());
    }
}
