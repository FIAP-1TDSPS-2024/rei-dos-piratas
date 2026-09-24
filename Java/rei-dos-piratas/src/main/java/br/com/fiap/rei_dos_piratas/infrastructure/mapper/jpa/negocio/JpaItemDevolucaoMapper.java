package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.ItemDevolucao;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaItemDevolucaoEntity;

public class JpaItemDevolucaoMapper {

	public static ItemDevolucao toEntity(JpaItemDevolucaoEntity entity) {
		return new ItemDevolucao(
				entity.getId(),
				JpaItemProdutoMapper.toEntity(entity.getItemPedido()),
				entity.getQuantidade()
		);
	}

	public static JpaItemDevolucaoEntity toJpaEntity(ItemDevolucao itemDevolucao) {
		return new JpaItemDevolucaoEntity(
				itemDevolucao.getId(),
				null,
				JpaItemProdutoMapper.toJpaPedidoProdutoReference(itemDevolucao.getItemPedido().getId()),
				itemDevolucao.getQuantidade()
		);
	}

	private JpaItemDevolucaoMapper() {
	}
}

