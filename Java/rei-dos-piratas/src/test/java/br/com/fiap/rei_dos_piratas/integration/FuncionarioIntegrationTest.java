package br.com.fiap.rei_dos_piratas.integration;

import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.repository.FuncionarioRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class FuncionarioIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Test
    void createCliente_shouldPersistAndReturnAuthor() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/funcionarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userName": "jonasDasNeves",
                                    "nomeCompleto": "Jonas da Silva Souza",
                                    "email": "jonas@example.com",
                                    "senha": "SenhaSegura1",
                                    "role": "ADMIN",
                                    "salario": 1000
                                  }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userName", is("jonasDasNeves")))
                .andExpect(jsonPath("$.email", is("jonas@example.com")));

        Page<Funcionario> vendedores = funcionarioRepository.listAll(0,10);
        assert vendedores.pageItems().get(0).getUserName().equals("jonasDasNeves");
    }
}
