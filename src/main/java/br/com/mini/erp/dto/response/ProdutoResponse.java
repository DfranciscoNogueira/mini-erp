package br.com.mini.erp.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "ProdutoResponse", description = "Resposta com os dados de um produto")
public record ProdutoResponse(

        @Schema(description = "Identificador único do produto",
                example = "1")
        Long id,

        @Schema(description = "SKU único do produto",
                example = "SKU-001")
        String sku,

        @Schema(description = "Nome do produto",
                example = "Camiseta Azul")
        String nome,

        @Schema(description = "Preço bruto unitário do produto",
                example = "59.90")
        BigDecimal precoBruto,

        @Schema(description = "Quantidade disponível em estoque",
                example = "50")
        Integer estoque,

        @Schema(description = "Estoque mínimo para alerta de reposição",
                example = "10")
        Integer estoqueMinimo,

        @Schema(description = "Indica se o produto está ativo para venda",
                example = "true")
        Boolean ativo
) {
}
