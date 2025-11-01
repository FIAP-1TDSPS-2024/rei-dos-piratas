package br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaClienteEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
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

    @OneToMany(mappedBy = "carrinho", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JpaProdutosCarrinhoEntity> produtosAdicionados;
}
