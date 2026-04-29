package br.com.fiap.rei_dos_piratas.integration;

import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.repository.FuncionarioRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    @WithMockUser(username = "admin", roles = {"WRITE_FUNCIONARIOS"})
    void createFuncionario_shouldPersistAndReturn() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/funcionarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userName": "jonasDasNeves",
                                    "nomeCompleto": "Jonas da Silva Souza",
                                    "email": "jonas@example.com",
                                    "senha": "SenhaSegura1",
                                    "perfil": "ADMIN",
                                    "salario": 1000
                                  }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userName", is("jonasDasNeves")))
                .andExpect(jsonPath("$.email", is("jonas@example.com")));

        Funcionario funcionarioCriado = funcionarioRepository.findByUsername("jonasDasNeves");
        assertNotNull(funcionarioCriado);
        assertEquals("jonas@example.com", funcionarioCriado.getEmail());
    }
}
