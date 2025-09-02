package br.com.mini.erp.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(name = "PedidoItemRequest", description = "Item do pedido com referência ao produto, quantidade e desconto opcional")
public record PedidoItemRequest(

        @Schema(description = "ID do produto", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Long produtoId,

        @Schema(description = "Quantidade do produto a ser incluída no pedido", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Min(1)
        Integer quantidade,

        @Schema(description = "Valor de desconto aplicado ao item (opcional)", example = "5.00")
        BigDecimal desconto
) { }


