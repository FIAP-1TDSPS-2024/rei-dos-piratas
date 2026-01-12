package br.com.fiap.rei_dos_piratas.infrastructure.entity.frete;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "TOKEN")
public class JpaTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "token", nullable = false)
    private String token;

    @Lob
    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    @Column(name = "data_criacao", nullable = false)
    private LocalDate dataCriacao;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDate dataExpiracao;
}
