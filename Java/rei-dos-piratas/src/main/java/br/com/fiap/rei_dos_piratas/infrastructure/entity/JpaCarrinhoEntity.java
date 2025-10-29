package br.com.fiap.rei_dos_piratas.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "CARRINHOS")
public class JpaCarrinhoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "carrinho")
    @JoinColumn(name = "cliente_id")
    private JpaClienteEntity cliente;

    @OneToMany(mappedBy = "carrinho")
    private List<JpaProdutosCarrinhoEntity> produtosAdicionados;
}
