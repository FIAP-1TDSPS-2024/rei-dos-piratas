package br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "MOTIVOS_DEVOLUCAO")
public class JpaMotivoDevolucaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60, unique = true)
    private String codigo;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(nullable = false)
    private Boolean arrependimento;

    @Column(nullable = false)
    private Boolean ativo;
}

