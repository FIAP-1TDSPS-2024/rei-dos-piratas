package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.FuncionarioService;
import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.repository.FuncionarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FuncionarioServiceImplTest {

    private FuncionarioService funcionarioService;
    private FuncionarioRepository funcionarioRepository;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        this.funcionarioRepository = mock(FuncionarioRepository.class);
        this.passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(any())).thenAnswer(invocation -> "encoded-") ;
        this.funcionarioService = new FuncionarioServiceImpl(funcionarioRepository, passwordEncoder);
    }

    @Test
    void listAll() {
        //O que
        Funcionario funcionario = new Funcionario(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                Role.USER,
                null,
                1000.00F);

        List<Funcionario> vendedores = new ArrayList<>();
        vendedores.add(funcionario);

        Page<Funcionario> vendedorPage = new Page<>(1, 0, vendedores);

        when(this.funcionarioRepository.findAllByUsuarioAtivoTrue(0,10))
                .thenReturn(vendedorPage);

        final Page<Funcionario> foundVendedorPage = this.funcionarioService.listAll(0,10);

        verify(this.funcionarioRepository, times(1))
                .findAllByUsuarioAtivoTrue(0,10);

        assertThat(foundVendedorPage).isSameAs(vendedorPage);
    }


    @Test
    void findById() {
        //O que
        Funcionario funcionario = new Funcionario(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                Role.USER,
                null,
                1000.00F);

        //Quando
        when(this.funcionarioRepository.findById(1L)).thenReturn(funcionario);

        //assert
        final Funcionario foundFuncionario = this.funcionarioService.findById(1L);
        verify(this.funcionarioRepository,times(1)).findById(any());
        assertThat(foundFuncionario).isSameAs(funcionario);
    }

    @Test
    void create() {
        //O que
        Funcionario funcionarioParaCriar = new Funcionario(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                Role.USER,
                null,
                1000.00F);

        Funcionario funcionarioCriado = new Funcionario(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                Role.USER,
                null,
                1000.00F);

        //Quando
        when(this.funcionarioRepository.create(funcionarioParaCriar)).thenReturn(funcionarioCriado);

        //assert
        final Funcionario newFuncionario = this.funcionarioService.create(funcionarioParaCriar);
        verify(this.funcionarioRepository,times(1)).create(any());
        assertThat(newFuncionario).isSameAs(funcionarioCriado);
    }

    @Test
    void update() {

        //O que
        Funcionario funcionarioAntigo = new Funcionario(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                Role.USER,
                null,
                1000.00F);

        Funcionario funcionarioNovo = new Funcionario(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                Role.USER,
                null,
                1000.00F);

        //Quando
        when(this.funcionarioRepository.findById(1L)).thenReturn(funcionarioAntigo);
        when(this.funcionarioRepository.update(funcionarioNovo)).thenReturn(funcionarioNovo);

        //assert
        final Funcionario newFuncionario = this.funcionarioService.update(funcionarioNovo);
        verify(this.funcionarioRepository,times(1)).update(any());
        assertThat(newFuncionario).isSameAs(funcionarioNovo).isNotSameAs(funcionarioAntigo);
    }

    @Test
    void ativarDesativar() {
        // Arrange
        Funcionario funcionario = new Funcionario(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                Role.USER,
                null,
                1000.00F);

        Funcionario funcionarioAtualizado = new Funcionario(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                false,
                LocalDate.now(),
                Role.USER,
                null,
                1000.00F);

        when(funcionarioRepository.findById(1L)).thenReturn(funcionario);
        when(funcionarioRepository.update(any(Funcionario.class))).thenReturn(funcionarioAtualizado);

        // Act
        Funcionario result = funcionarioService.ativarDesativar(1L);

        // Assert
        verify(funcionarioRepository, times(1)).findById(1L);
        verify(funcionarioRepository, times(1)).update(any(Funcionario.class));
        assertThat(result.isUsuarioAtivo()).isFalse();
    }
}