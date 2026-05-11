package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.application.service.FreteService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.EstoqueInsuficienteException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.WrongStatusException;
import br.com.fiap.rei_dos_piratas.domain.repository.DadosEmpresaRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PedidoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.security.CustomUserDetails;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.FreteServiceDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.etiqueta.GeracaoEtiquetasResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pagamento.CompraFreteResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository repository;

    private final ProdutoRepository produtoRepository;

    private final ClienteService clienteService;

    private final EnderecoService enderecoService;
    
    private final DadosEmpresaRepository dadosEmpresaRepository;

    private final FreteService freteService;

    public PedidoServiceImpl(PedidoRepository repository, ProdutoRepository produtoRepository, ClienteService clienteService, EnderecoService enderecoService, DadosEmpresaRepository dadosEmpresaRepository, FreteService freteService) {
        this.repository = repository;
        this.produtoRepository = produtoRepository;
        this.clienteService = clienteService;
        this.enderecoService = enderecoService;
        this.dadosEmpresaRepository = dadosEmpresaRepository;
        this.freteService = freteService;
    }

    @Override
    public Page<Pedido> findAll(int pageNumber, int pageSize) {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //Verifica se usuário é um funcionário procurando uma ROLE comum a todos
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("PEDIDO_WRITE"))) {
            return this.repository.listAll(
                                pageNumber,
                                pageSize);

            }
            else {
                return this.repository.listAllByClient(
                                pageNumber,
                                pageSize,
                                userDetails.getId());
            }
    }

    @Override
    public Page<Pedido> findAllByStatus(int pageNumber, int pageSize, StatusEnum status) {
        return this.repository.listAllByStatus(pageNumber, pageSize, status);
    }

    @Override
    public Pedido findById(Long id) {
        try {
            return this.repository.findById(id);
        }
        catch (NoSuchElementException e){
            throw new ResourceNotFoundException("Não foi possível encontrar um pedido com o id " + id);
        }
    }

    @Transactional
    @Override
    public Pedido fazerPedido(Pedido pedido) {

        List<FreteServiceDto> fretes = this.freteService.calcularFreteProdutos(
                pedido.getEnderecoEntrega().getCep(),
                pedido.getProdutosAdicionados());

        Optional<FreteServiceDto> consultaFrete = fretes
                .stream()
                .findFirst()
                .filter(frete -> frete.id().equals(pedido.getServicoEntrega()));

        if (consultaFrete.isEmpty()){
            throw new ResourceNotFoundException("Esse serviço de entrega não existe para esse serviço");
        }

        //Após consulta se ID de frete existe, o valor do frete é definido
        //Dessa forma, evita-se adulteração de preço ou preços desatualizados
        pedido.setValorFrete(consultaFrete.get().price());

        //Definição de valor total depois de definição de valor do frete
        pedido.setValorTotal(calcularValorTotalPedido(pedido));

        this.verificaEAtualizaEstoqueparaPedido(pedido);
        return this.repository.create(pedido);
    }

    @Transactional
    @Override
    public Pedido pagarPedido(Long id) {
        //Busca pedido por ID
        Pedido pedido = this.findById(id);

        //Verifica o status do pedido para
        if (pedido.getStatus() == StatusEnum.AGUARDANDO_PAGAMENTO){

            pedido.setStatus(StatusEnum.PREPARANDO_ENVIO);

            //Criação de pedido de frete é feito à API melhor envio após confirmado o pagamento
            PedidoFreteRequestDto pedidoFrete = this.montarPedidoFreteDto(pedido);
            PedidoFreteResponseDto responsePedidoFrete = this.freteService.criarPedidoFrete(pedidoFrete);
            pedido = this.enriquecerPedidoPorFrete(pedido, responsePedidoFrete);

            return this.repository.update(pedido);
        }
        else {
            throw new WrongStatusException(
                    "O pedido deve estar no estado " + StatusEnum.AGUARDANDO_PAGAMENTO +
                    "para ser pago, mas ele está no estado " + pedido.getStatus());
        }
    }

    @Transactional
    @Override
    public String organizarPedidosParaEnvio(List<Long> pedidos) {

        // Busca todos os pedidos com status PREPARANDO_ENVIO em uma única consulta
        List<Pedido> pedidosParaOrganizacao = this.repository.findByIdsAndStatus(
                pedidos,
                StatusEnum.PREPARANDO_ENVIO
        );

        // Se nenhum pedido válido foi encontrado, retorna mensagem
        if (pedidosParaOrganizacao.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum pedido encontrado com status PREPARANDO_ENVIO na lista passada");
        }

        // Extrai os IDs dos pedidos de frete
        List<String> pedidosFrete = pedidosParaOrganizacao
                .stream()
                .map(pedido -> pedido.getPedidoFrete().toString())
                .toList();

        // Chama o serviço de frete
        CompraFreteResponseDto response = this.freteService.organizarPedidoFrete(pedidosFrete);

        if (response.message() != null) {
            return response.message();
        } else {
            // Atualiza todos os pedidos em lote para o novo status
            List<Long> idsParaAtualizar = pedidosParaOrganizacao
                    .stream()
                    .map(Pedido::getId)
                    .toList();

            this.repository.updateStatusBatch(idsParaAtualizar, StatusEnum.AGUARDANDO_GERACAO_ETIQUETA);

            return null;
        }
    }

    @Override
    public Map<Long, String> gerarEtiquetasParaEnvio(List<Long> pedidos) {
        // Busca todos os pedidos com status AGUARDANDO_GERACAO_ETIQUETA em uma única consulta
        List<Pedido> pedidosParaOrganizacao = this.repository.findByIdsAndStatus(
                pedidos,
                StatusEnum.AGUARDANDO_GERACAO_ETIQUETA
        );

        // Se nenhum pedido válido foi encontrado, retorna mapa vazio
        if (pedidosParaOrganizacao.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum pedido encontrado com status AGUARDANDO_ETIQUETA na lista passada");
        }

        // Cria mapeamento de UUID frete -> ID pedido para correlacionar as respostas
        Map<String, Long> freteParaPedidoMap = pedidosParaOrganizacao
                .stream()
                .collect(HashMap::new,
                    (map, pedido) -> map.put(pedido.getPedidoFrete().toString(), pedido.getId()),
                    HashMap::putAll);

        // Extrai os IDs dos pedidos de frete
        List<String> pedidosFrete = pedidosParaOrganizacao
                .stream()
                .map(pedido -> pedido.getPedidoFrete().toString())
                .toList();

        // Chama o serviço de frete
        GeracaoEtiquetasResponseDto response = this.freteService.gerarEtiquetasPedidoFrete(pedidosFrete);

        // Mapeamento do resultado: converte UUID frete -> mensagem para ID pedido -> mensagem
        Map<Long, String> resultado = new HashMap<>();
        List<Long> pedidosComSucesso = new ArrayList<>();

        if (response.pedidos() != null && !response.pedidos().isEmpty()) {
            // Mapeia as mensagens específicas de cada pedido de frete para o ID do pedido interno
            response.pedidos().forEach((freteId, statusEtiqueta) -> {
                Long pedidoId = freteParaPedidoMap.get(freteId);
                if (pedidoId != null) {
                    if (statusEtiqueta.status()) {
                        resultado.put(pedidoId, "Etiqueta gerada com sucesso");
                        pedidosComSucesso.add(pedidoId);
                    } else {
                        resultado.put(pedidoId, "Erro ao gerar etiqueta: " + statusEtiqueta.message());
                    }
                }
            });
        } else {
            // Fallback para erro geral quando não há informações específicas dos pedidos
            pedidosParaOrganizacao.forEach(pedido ->
                resultado.put(pedido.getId(), "Erro ao processar geração de etiquetas")
            );
        }

        // Atualiza status dos pedidos que tiveram etiquetas geradas com sucesso
        if (!pedidosComSucesso.isEmpty()) {
            this.repository.updateStatusBatch(pedidosComSucesso, StatusEnum.AGUARDANDO_POSTAGEM);
        }

        return resultado;
    }

    @Override
    public String imprimirEtiquetasEnvio(List<Long> pedidos) {
        // Busca todos os pedidos com status AGUARDANDO_POSTAGEM em uma única consulta
        List<Pedido> pedidosParaImpressao = this.repository.findByIdsAndStatus(
                pedidos,
                StatusEnum.AGUARDANDO_POSTAGEM
        );

        // Se nenhum pedido válido foi encontrado, retorna mensagem
        if (pedidosParaImpressao.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum pedido válido encontrado para impressão na lista passada");
        }

        // Extrai os IDs dos pedidos de frete
        List<String> pedidosFrete = pedidosParaImpressao
                .stream()
                .map(pedido -> pedido.getPedidoFrete().toString())
                .toList();

        // Chama o serviço de frete e retorna apenas a URL
        return this.freteService.imprimirEtiquetasPedidoFrete(pedidosFrete).url();
    }

    @Transactional
    @Override
    public Pedido enviarPedido(Long id) {
        //Busca pedido por ID
        Pedido pedido = this.findById(id);

        //Verifica se o pedido está cancelado
        if (pedido.getStatus() == StatusEnum.AGUARDANDO_POSTAGEM){
            pedido.setStatus(StatusEnum.EM_TRANSITO);
            return this.repository.update(pedido);
        }
        else {
            throw new WrongStatusException(
                    "O pedido deve estar no estado " + StatusEnum.AGUARDANDO_POSTAGEM +
                    " para ser enviado para entrega, mas ele está no estado " + pedido.getStatus());
        }
    }

    @Transactional
    @Override
    public Pedido entregarPedido(Long id) {
        //Busca pedido por ID
        Pedido pedido = this.findById(id);

        //Verifica se o pedido está cancelado
        if (pedido.getStatus() == StatusEnum.EM_TRANSITO){
            pedido.setStatus(StatusEnum.ENTREGUE);
            return this.repository.update(pedido);
        }
        else {
            throw new WrongStatusException(
                    "O pedido deve estar no estado " + StatusEnum.EM_TRANSITO +
                    " para ser entregue, mas ele está no estado " + pedido.getStatus());
        }
    }

    @Transactional
    @Override
    public Pedido cancelarPedido(Long id) {
        //Busca pedido por ID
        Pedido pedido = this.findById(id);

        //Verifica se o pedido está cancelado
        if (pedido.getStatus() == StatusEnum.CANCELADO){
            throw new WrongStatusException("O pedido já está cancelado.");
        }
        else if (pedido.getStatus() == StatusEnum.ENTREGUE) {
            throw new WrongStatusException("O pedido já foi entregue");
        }
        else if ((pedido.getStatus() == StatusEnum.AGUARDANDO_PAGAMENTO) || pedido.getStatus() == StatusEnum.PREPARANDO_ENVIO) {
            //Retorna os produtos para estoque
            pedido.getProdutosAdicionados()
                    .forEach(produto -> {
                        produto
                                .getProduto()
                                .setEstoque(
                                        produto.getProduto().getEstoque() +
                                        produto.getQuantidade());

                        this.produtoRepository.update(produto.getProduto());
                    });
            pedido.setStatus(StatusEnum.CANCELADO);
            return this.repository.update(pedido);
        }
        else {
            throw new WrongStatusException("O pedido já foi tramitado. Para fazer o cancelamento, deve ser solicitada uma devolução.");
        }
    }

    private void verificaEAtualizaEstoqueparaPedido(Pedido pedido) {
        //Verificar se os todos os produtos tem estoque suficiente para lançar exceção se não
        pedido.getProdutosAdicionados()
                .forEach(produto -> {
                    if (produto.getProduto().getEstoque() < produto.getQuantidade()) {
                        throw new EstoqueInsuficienteException("Estoque insuficiente! O produto " +
                                produto.getProduto().getNome() + " de ID " +
                                produto.getProduto().getId() + " tem apenas " +
                                produto.getProduto().getEstoque() + " unidades em estoque.");
                    }
                });

        //Se todos os itens estiverem OK, o estoque é editado
        pedido.getProdutosAdicionados()
                .forEach(produto -> {
                    //Faz subtração do estoque se for possível
                    produto.getProduto().setEstoque(produto.getProduto().getEstoque() - produto.getQuantidade());
                    //Salva novo estoque em banco
                    this.produtoRepository.update(produto.getProduto());
                });
    }

    //Método para enriquecemento de pedido com informações do pedido de frete
    //TODO Verificar informações necessárias na response da API e cortar o restante do DTO
    private Pedido enriquecerPedidoPorFrete(Pedido pedido, PedidoFreteResponseDto pedidoFreteResponseDto) {
        pedido.setPedidoFrete(pedidoFreteResponseDto.id());
        pedido.setDataPrevisaoEntrega(LocalDate.now().plusDays(pedidoFreteResponseDto.deliveryMax()));
        return this.repository.update(pedido);
    }

    private PedidoFreteRequestDto montarPedidoFreteDto(Pedido pedido) {
        //Definir empresa como remetente
        Endereco enderecoEmpresa = this.enderecoService.getEnderecoEmpresa();
        DadosEmpresa dadosEmpresa = this.dadosEmpresaRepository.get();
        DestinoRemetenteDto remetente = this.definirRemetenteEmpresa(enderecoEmpresa, dadosEmpresa);

        //Definir cliente como destino
        Endereco enderecoDestino = this.enderecoService.findById(pedido.getEnderecoEntrega().getId());
        Cliente cliente = pedido.getCliente();
        DestinoRemetenteDto destino = this.definirDestinoEntrega(enderecoDestino, cliente);

        //Definir lista de produtos e quantidade
        List<ProdutoPedidoFreteDto> produtos = this.definirProdutosFrete(pedido);

        //Definir dimensões do(s) volume(s) para entrega
        List<VolumeFreteDto> volumes = new ArrayList<>();
        volumes.add(definirDimensoesVolumes(pedido));

        //Definir opções de entrega:
        FreteOptionsDto opcoes = new FreteOptionsDto(
                "Rei dos Piratas",
                "Entrega do pedido " + pedido.getId(),
                pedido.getValorTotal(),
                false,
                false,
                false,
                false,
                null);

        return new PedidoFreteRequestDto(
                pedido.getServicoEntrega(),
                remetente,
                destino,
                produtos,
                volumes,
                opcoes);
    }

    private DestinoRemetenteDto definirRemetenteEmpresa(Endereco enderecoEmpresa, DadosEmpresa dadosEmpresa) {
        return new DestinoRemetenteDto(
                dadosEmpresa.getNomeFantasia(),
                dadosEmpresa.getEmail(),
                dadosEmpresa.getTelefone(),
                null,
                dadosEmpresa.getCnpj(),
                dadosEmpresa.getStateRegister(),
                dadosEmpresa.getEconomicActivityCode(),
                enderecoEmpresa.getLogradouro(),
                null,
                String.valueOf(enderecoEmpresa.getNumero()),
                enderecoEmpresa.getBairro(),
                enderecoEmpresa.getCidade().getNome(),
                enderecoEmpresa.getCep(),
                enderecoEmpresa.getCidade().getEstado().getSigla());
    }

    private DestinoRemetenteDto definirDestinoEntrega(Endereco enderecoDestino, Cliente cliente) {
        return new DestinoRemetenteDto(
                cliente.getNomeCompleto(),
                cliente.getEmail(),
                cliente.getCelular(),
                cliente.getCpf(),
                null,
                null,
                null,
                enderecoDestino.getLogradouro(),
                null,
                String.valueOf(enderecoDestino.getNumero()),
                enderecoDestino.getBairro(),
                enderecoDestino.getCidade().getNome(),
                enderecoDestino.getCep(),
                enderecoDestino.getCidade().getEstado().getSigla());
    }

    private List<ProdutoPedidoFreteDto> definirProdutosFrete(Pedido pedido) {
        return pedido.getProdutosAdicionados()
                .stream()
                .map(produto -> new ProdutoPedidoFreteDto(
                        produto.getProduto().getNome(),
                        String.valueOf(produto.getQuantidade()),
                        String.valueOf(produto.getProduto().getPreco())))
                .toList();
    }

    private VolumeFreteDto definirDimensoesVolumes(Pedido pedido) {
        int quantidadeTotal = pedido.getProdutosAdicionados().stream()
                .mapToInt(ItemProdutoPedido::getQuantidade)
                .sum();

        BigDecimal pesoTotal = pedido.getProdutosAdicionados().stream()
                .map(produto -> produto.getProduto().getPeso().multiply(BigDecimal.valueOf(produto.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (quantidadeTotal < 6) {
            int altura = pedido.getProdutosAdicionados().stream()
                    .mapToInt(produto -> produto.getProduto().getProfundidade().intValue() * produto.getQuantidade())
                    .sum() + 3;

            return new VolumeFreteDto(altura, 16, 26, pesoTotal);
        }

        if (quantidadeTotal < 11) {
            return new VolumeFreteDto(28, 16, 26, pesoTotal);
        }

        if (quantidadeTotal < 20) {
            return new VolumeFreteDto(28, 32, 26, pesoTotal);
        }

        return new VolumeFreteDto(28, 32, 52, pesoTotal);
    }

    private BigDecimal calcularValorTotalPedido(Pedido pedido){

        if (pedido.getValorFrete() == null){
            throw new IllegalArgumentException("Consulte o valor do frete do pedido antes do cálculo de valor total");
        }

        //O valor total do pedido é calculado somando os valores dos produtos, multplicando por sua quantidade e por fim adicionando o valor do frete
        return pedido.getProdutosAdicionados()
                .stream()
                .map(item -> item
                        .getProduto()
                        .getPreco()
                        .multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(pedido.getValorFrete());
    }
}
