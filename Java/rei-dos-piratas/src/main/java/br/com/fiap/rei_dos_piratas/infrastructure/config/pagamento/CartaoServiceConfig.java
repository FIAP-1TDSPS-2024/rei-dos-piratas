package br.com.fiap.rei_dos_piratas.infrastructure.config.pagamento;

import br.com.fiap.rei_dos_piratas.application.service.CartaoService;
import br.com.fiap.rei_dos_piratas.application.service.UsuarioAtualService;
import br.com.fiap.rei_dos_piratas.application.service.impl.CartaoServiceImpl;
import br.com.fiap.rei_dos_piratas.domain.entity.RegraParcelamento;
import br.com.fiap.rei_dos_piratas.domain.repository.CartaoClienteRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.CobrancaAppClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class CartaoServiceConfig {

    @Bean
    public CartaoService cartaoService(CartaoClienteRepository cartaoClienteRepository, CobrancaAppClient apiCobranca, UsuarioAtualService usuarioAtual) {
        return new CartaoServiceImpl(cartaoClienteRepository, apiCobranca, usuarioAtual);
    }

    @Bean
    public RegraParcelamento regraParcelamento(@Value("${app.pagamento.parcelamento.valor-minimo}") BigDecimal valorMinimo,
                                               @Value("${app.pagamento.parcelamento.max-parcelas}") int maxParcelas) {
        return new RegraParcelamento(valorMinimo, maxParcelas);
    }
}
