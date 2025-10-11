package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.VendedorService;
import br.com.fiap.rei_dos_piratas.application.service.impl.VendedorServiceImpl;
import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Vendedor;
import br.com.fiap.rei_dos_piratas.domain.repository.VendedorRepository;
import br.com.fiap.rei_dos_piratas.interfaces.controller.VendedorController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VendedorControllerImplTest {

    private VendedorService vendedorService;
    private VendedorController vendedorController;

    @BeforeEach
    void setUp() {
        this.vendedorService = mock(VendedorService.class);
        this.vendedorController = new VendedorControllerImpl(vendedorService);
    }

    @Test
    void listAll() {
        //O que
        Vendedor vendedor = new Vendedor(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                Role.USER);

        List<Vendedor> vendedores = new ArrayList<Vendedor>();
        vendedores.add(vendedor);

        Page<Vendedor> vendedorPage = new Page<Vendedor>(1, 0, vendedores);

        when(this.vendedorService.listAll(0,10)).thenReturn(vendedorPage);

        final Page<Vendedor> foundVendedorPage = this.vendedorService.listAll(0,10);
        verify(this.vendedorService, times(1)).listAll(0,10);
        assertThat(foundVendedorPage).isSameAs(vendedorPage);
    }

    @Test
    void findById() {
        //O que
        Vendedor vendedor = new Vendedor(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                Role.USER);

        //Quando
        when(this.vendedorService.findById(1L)).thenReturn(vendedor);

        //assert
        final Vendedor foundVendedor = this.vendedorService.findById(1L);
        verify(this.vendedorService,times(1)).findById(any());
        assertThat(foundVendedor).isSameAs(vendedor);
    }

    @Test
    void create() {
        //O que
        Vendedor vendedorParaCriar = new Vendedor(
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                Role.USER);

        Vendedor vendedorCriado = new Vendedor(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                Role.USER);

        //Quando
        when(this.vendedorService.create(vendedorParaCriar)).thenReturn(vendedorCriado);

        //assert
        final Vendedor newVendedor = this.vendedorService.create(vendedorParaCriar);
        verify(this.vendedorService,times(1)).create(any());
        assertThat(newVendedor).isSameAs(vendedorCriado);
    }

    @Test
    void update() {

        //O que
        Vendedor vendedorAntigo = new Vendedor(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                Role.USER);

        Vendedor vendedorNovo = new Vendedor(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                Role.USER);

        //Quando
        when(this.vendedorService.findById(1L)).thenReturn(vendedorAntigo);
        when(this.vendedorService.update(vendedorNovo)).thenReturn(vendedorNovo);

        //assert
        final Vendedor newVendedor = this.vendedorService.update(vendedorNovo);
        verify(this.vendedorService,times(1)).update(any());
        assertThat(newVendedor).isSameAs(vendedorNovo).isNotSameAs(vendedorAntigo);
    }

    @Test
    void delete() {
        // Quando
        doNothing().when(vendedorService).delete(1L);

        // Executa
        vendedorService.delete(1L);

        // Verifica
        verify(vendedorService, times(1)).delete(1L);
    }
}