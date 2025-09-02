package br.com.mini.erp.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(name = "ProdutoRequest", description = "Requisição para criação ou atualização de produtos")
public record ProdutoRequest(

        @Schema(description = "SKU único do produto",
                example = "SKU-001",
                maxLength = 64,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 64)
        String sku,

        @Schema(description = "Nome do produto",
                example = "Camiseta Azul",
                maxLength = 150,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 150)
        String nome,

        @Schema(description = "Preço bruto unitário do produto",
                example = "59.90",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @DecimalMin("0.00")
        BigDecimal precoBruto,

        @Schema(description = "Quantidade disponível em estoque",
                example = "50",
                minimum = "0",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Min(0)
        Integer estoque,

        @Schema(description = "Estoque mínimo para alerta de reposição",
                example = "10",
                minimum = "0",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Min(0)
        Integer estoqueMinimo,

        @Schema(description = "Define se o produto está ativo para venda",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Boolean ativo
) {
}

