package br.com.fiap.rei_dos_piratas.integration;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class ClienteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    void createCliente_shouldPersistAndReturnAuthor() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "user_name": "jonasDasNeves",
                                   "nome_completo": "Jonas da Silva Souza",
                                   "email": "jonas@example.com",
                                   "senha": "SenhaSegura",
                                   "data_nascimento": "2004-03-13",
                                   "sexo": "M",
                                   "cpf": "52998224725",
                                   "celular": "11991231234"
                                 }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cliente.id").exists())
                .andExpect(jsonPath("$.cliente.user_name", is("jonasDasNeves")))
                .andExpect(jsonPath("$.cliente.email", is("jonas@example.com")));

        Page<Cliente> clientes = clienteRepository.listAll(0,10);
        assert clientes.pageItems().get(0).getUsername().equals("jonasDasNeves");
    }
}
