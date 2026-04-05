package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.FuncionarioService;
import br.com.fiap.rei_dos_piratas.domain.Enum.PerfilEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Perfil;
import br.com.fiap.rei_dos_piratas.interfaces.controller.FuncionarioController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.FuncionarioInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.FuncionarioOutDto;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.usuarios.FuncionarioDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FuncionarioControllerImplTest {

    private FuncionarioService funcionarioService;
    private FuncionarioController funcionarioController;
    private Perfil perfilPadrao;

    @BeforeEach
    void setUp() {
        this.funcionarioService = mock(FuncionarioService.class);
        this.funcionarioController = new FuncionarioControllerImpl(funcionarioService);
        this.perfilPadrao = new Perfil(1L, "FUNCIONARIO", "Perfil de funcionário", null);
    }

    @Test
    void listAll() {
        // Arrange
        Funcionario funcionario = new Funcionario(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                perfilPadrao,
                null,
                BigDecimal.valueOf(1000));

        List<Funcionario> funcionarios = new ArrayList<>();
        funcionarios.add(funcionario);
        Page<Funcionario> funcionarioPage = new Page<>(1, 0, funcionarios);

        when(this.funcionarioService.listAll(0, 10)).thenReturn(funcionarioPage);

        // Act
        final Page<FuncionarioOutDto> result = this.funcionarioController.listAll(0, 10);

        // Assert
        verify(this.funcionarioService, times(1)).listAll(0, 10);
        assertThat(result).isNotNull();
        assertThat(result.pageItems()).hasSize(1);
    }

    @Test
    void findById() {
        // Arrange
        Funcionario funcionario = new Funcionario(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                perfilPadrao,
                null,
                BigDecimal.valueOf(1000));

        when(this.funcionarioService.findById(1L)).thenReturn(funcionario);

        // Act
        final FuncionarioOutDto result = this.funcionarioController.findById(1L);

        // Assert
        verify(this.funcionarioService, times(1)).findById(1L);
        assertThat(result).isNotNull();
    }

    @Test
    void create() {
        // Arrange
        FuncionarioInDto funcionarioInDto = new FuncionarioInDto(
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                PerfilEnum.USER,
                BigDecimal.valueOf(1000));

        Funcionario funcionarioCriado = new Funcionario(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                perfilPadrao,
                null,
                BigDecimal.valueOf(1000));

        when(this.funcionarioService.create(any(Funcionario.class))).thenReturn(funcionarioCriado);

        // Act
        final FuncionarioOutDto result = this.funcionarioController.create(funcionarioInDto);

        // Assert
        verify(this.funcionarioService, times(1)).create(any(Funcionario.class));
        assertThat(result).isNotNull();
    }

    @Test
    void update() {
        // Arrange
        FuncionarioInDto funcionarioInDto = new FuncionarioInDto(
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                PerfilEnum.USER,
                BigDecimal.valueOf(1200));

        Funcionario funcionarioAtualizado = new Funcionario(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                perfilPadrao,
                null,
                BigDecimal.valueOf(1200));

        when(this.funcionarioService.update(any(Funcionario.class))).thenReturn(funcionarioAtualizado);

        // Act
        Funcionario funcionario = FuncionarioDtoMapper.toEntity(funcionarioInDto);
        final FuncionarioOutDto result = this.funcionarioController.update(funcionario);

        // Assert
        verify(this.funcionarioService, times(1)).update(any(Funcionario.class));
        assertThat(result).isNotNull();
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
                false,
                LocalDate.now(),
                perfilPadrao,
                null,
                BigDecimal.valueOf(1000));

        when(funcionarioService.ativarDesativar(1L)).thenReturn(funcionario);

        // Act
        FuncionarioOutDto result = funcionarioController.ativarDesativar(1L);

        // Assert
        verify(funcionarioService, times(1)).ativarDesativar(1L);
        assertThat(result).isNotNull();
    }
}