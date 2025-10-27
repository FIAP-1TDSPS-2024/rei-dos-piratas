package br.com.fiap.rei_dos_piratas.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "CARRINHOS")
public class JpaCarrinhoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private JpaClienteEntity cliente;

    @OneToMany(mappedBy = "carrinho")
    private List<JpaProdutosCarrinhoEntity> produtosAdicionados;
}
