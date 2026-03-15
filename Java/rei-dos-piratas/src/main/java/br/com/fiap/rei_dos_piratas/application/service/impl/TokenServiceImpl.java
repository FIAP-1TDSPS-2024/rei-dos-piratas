package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.TokenService;
import br.com.fiap.rei_dos_piratas.domain.entity.Token;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.TokenRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.FreteTokenClient;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.token.TokenRequestDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.token.TokenResponseDto;

public class TokenServiceImpl implements TokenService {

    private final TokenRepository repository;
    private final FreteTokenClient apiFrete;

    public TokenServiceImpl(TokenRepository repository, FreteTokenClient apiFrete) {
        this.repository = repository;
        this.apiFrete = apiFrete;
    }

    @Override
    public Token findLastToken() {
        try{
            Token token = repository.findLastToken();

            if (token.isTokenValid()) {
                return token;
            }
            else{
                return this.gerarNovoToken(token.getRefreshToken());
            }
        }
        catch (Exception e){
            throw new ResourceNotFoundException("Não é possível conectar nos serviços de frete. " + e.getMessage());
        }
    }

    @Override
    public Token gerarNovoToken(String refreshToken) {
        //Definição de informações de cliente melhor envio para renovação de token
        String clientId = System.getenv("ME_CLIENT_ID");
        String clientSecret = System.getenv("ME_SECRET");
        String redirectUri = System.getenv("ME_REDIRECT_URI");

        //Criar objeto para request
        TokenRequestDto dto = new TokenRequestDto("refresh_token", clientId, clientSecret, redirectUri, refreshToken);

        //Chamada de API melhor envio
        TokenResponseDto tokenResponse = this.apiFrete.renovarToken(dto);

        //Retorno e salvamento do novo Token
        Token token = new Token(tokenResponse.access_token(), tokenResponse.refresh_token(), tokenResponse.expires_in());
        return this.repository.save(token);
    }
}
