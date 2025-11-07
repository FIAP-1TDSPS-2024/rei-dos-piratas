package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.FuncionarioService;
import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.interfaces.controller.FuncionarioController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FuncionarioControllerImplTest {

    private FuncionarioService funcionarioService;
    private FuncionarioController funcionarioController;

    @BeforeEach
    void setUp() {
        this.funcionarioService = mock(FuncionarioService.class);
        this.funcionarioController = new FuncionarioControllerImpl(funcionarioService);
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

        List<Funcionario> vendedores = new ArrayList<Funcionario>();
        vendedores.add(funcionario);

        Page<Funcionario> vendedorPage = new Page<Funcionario>(1, 0, vendedores);

        when(this.funcionarioService.listAll(0,10)).thenReturn(vendedorPage);

        final Page<Funcionario> foundVendedorPage = this.funcionarioService.listAll(0,10);
        verify(this.funcionarioService, times(1)).listAll(0,10);
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
        when(this.funcionarioService.findById(1L)).thenReturn(funcionario);

        //assert
        final Funcionario foundFuncionario = this.funcionarioService.findById(1L);
        verify(this.funcionarioService,times(1)).findById(any());
        assertThat(foundFuncionario).isSameAs(funcionario);
    }

    @Test
    void create() {
        //O que
        Funcionario funcionarioParaCriar = new Funcionario(
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                Role.USER,
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
        when(this.funcionarioService.create(funcionarioParaCriar)).thenReturn(funcionarioCriado);

        //assert
        final Funcionario newFuncionario = this.funcionarioService.create(funcionarioParaCriar);
        verify(this.funcionarioService,times(1)).create(any());
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
        when(this.funcionarioService.findById(1L)).thenReturn(funcionarioAntigo);
        when(this.funcionarioService.update(funcionarioNovo)).thenReturn(funcionarioNovo);

        //assert
        final Funcionario newFuncionario = this.funcionarioService.update(funcionarioNovo);
        verify(this.funcionarioService,times(1)).update(any());
        assertThat(newFuncionario).isSameAs(funcionarioNovo).isNotSameAs(funcionarioAntigo);
    }

    @Test
    void ativarDesativar() {
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
                false,
                LocalDate.now(),
                Role.USER,
                null,
                1000.00F);

        // Quando
        when(funcionarioService.ativarDesativar(1L)).thenReturn(funcionarioNovo);

        // Executa
        Funcionario newFuncionario = funcionarioService.ativarDesativar(1L);

        // Verifica
        verify(funcionarioService, times(1)).ativarDesativar(1L);
        assertThat(newFuncionario).isSameAs(funcionarioNovo).isNotSameAs(funcionarioAntigo);
    }
}