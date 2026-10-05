package br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "AUTH_SESSOES")
@Getter
@Setter
@NoArgsConstructor
public class JpaSessaoEntity {
    @Id private UUID id;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conta", nullable = false) private TipoConta tipo;
    @Column(name = "usuario_id", nullable = false) private Long usuarioId;
    @Column(name = "criada_em", nullable = false) private Instant criadaEm;
    @Column(name = "expira_em", nullable = false) private Instant expiraEm;
    @Column(name = "revogada_em") private Instant revogadaEm;
    @Column(name = "refresh_hash", nullable = false, length = 64, unique = true) private String refreshHash;
    @ElementCollection
    @CollectionTable(name = "AUTH_REFRESH_USADOS", joinColumns = @JoinColumn(name = "sessao_id"))
    @Column(name = "refresh_hash", nullable = false, length = 64)
    private Set<String> refreshUsados = new HashSet<>();
}
