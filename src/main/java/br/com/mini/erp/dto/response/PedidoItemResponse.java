package br.com.mini.erp.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "PedidoItemResponse", description = "Item que compõe um pedido")
public record PedidoItemResponse(

        @Schema(description = "Identificador do produto",
                example = "10")
        Long produtoId,

        @Schema(description = "SKU do produto",
                example = "SKU-001")
        String sku,

        @Schema(description = "Nome do produto",
                example = "Camiseta Azul")
        String nome,

        @Schema(description = "Quantidade do produto nesse pedido",
                example = "2")
        Integer quantidade,

        @Schema(description = "Preço unitário do produto no momento do pedido",
                example = "59.90")
        BigDecimal precoUnitario,

        @Schema(description = "Valor de desconto aplicado a esse item (se houver)",
                example = "5.00")
        BigDecimal desconto,

        @Schema(description = "Valor total desse item após aplicar desconto",
                example = "114.80")
        BigDecimal totalLinha
) {
}
