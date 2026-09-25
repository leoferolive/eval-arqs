package com.example.loja.dto;

import com.example.loja.model.ItemPedido;
import com.example.loja.model.Pedido;
import com.example.loja.model.Produto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class Dtos {
    private Dtos() {}

    private static BigDecimal money(BigDecimal v) { return v.setScale(2, RoundingMode.HALF_UP); }

    public record NovoProduto(@NotBlank @Size(max = 30) String sku, @NotBlank @Size(max = 120) String nome,
                              @NotNull @Positive BigDecimal preco, @NotNull @PositiveOrZero Integer estoque) {}

    public record AtualizaProduto(@NotBlank @Size(max = 120) String nome, @NotNull @Positive BigDecimal preco,
                                  @NotNull @PositiveOrZero Integer estoque, @NotNull Boolean ativo) {}

    public record ProdutoResponse(Long id, String sku, String nome, BigDecimal preco, int estoque, boolean ativo) {
        public static ProdutoResponse of(Produto p) {
            return new ProdutoResponse(p.getId(), p.getSku(), p.getNome(), money(p.getPreco()), p.getEstoque(), p.isAtivo());
        }
    }

    public record NovoItem(@NotNull Long produtoId, @NotNull @Min(1) Integer quantidade) {}

    public record NovoPedido(@NotNull @Pattern(regexp = "\\d{8}") String cep, @NotEmpty List<@Valid @NotNull NovoItem> itens) {}

    public record ItemResponse(Long produtoId, int quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {
        static ItemResponse of(ItemPedido i) {
            return new ItemResponse(i.getProdutoId(), i.getQuantidade(), money(i.getPrecoUnitario()), money(i.subtotal()));
        }
    }

    public record PedidoResponse(Long id, String cep, String status, List<ItemResponse> itens, BigDecimal valorItens,
                                 BigDecimal valorFrete, int prazoEntregaDias, BigDecimal valorTotal) {
        public static PedidoResponse of(Pedido p) {
            return new PedidoResponse(p.getId(), p.getCep(), p.getStatus().name(),
                    p.getItens().stream().map(ItemResponse::of).toList(), money(p.valorItens()),
                    money(p.getValorFrete()), p.getPrazoEntregaDias(), money(p.valorItens().add(p.getValorFrete())));
        }
    }

    public record Frete(BigDecimal valor, int prazoDias) {}

    public record Erro(String codigo, String mensagem) {}
}
