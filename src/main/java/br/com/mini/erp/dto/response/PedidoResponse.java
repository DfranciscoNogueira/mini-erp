package br.com.mini.erp.dto.response;

import br.com.mini.erp.enuns.PedidoStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(name = "PedidoResponse", description = "Resposta com os dados completos de um pedido")
public record PedidoResponse(

        @Schema(description = "Identificador único do pedido",
                example = "1001")
        Long id,

        @Schema(description = "Identificador do cliente que realizou o pedido",
                example = "1")
        Long clienteId,

        @Schema(description = "Status atual do pedido",
                example = "CREATED")
        PedidoStatus status,

        @Schema(description = "Subtotal do pedido (soma dos itens antes de descontos)",
                example = "119.80")
        BigDecimal subtotal,

        @Schema(description = "Valor total de descontos aplicados ao pedido",
                example = "5.00")
        BigDecimal descontos,

        @Schema(description = "Valor final do pedido após descontos",
                example = "114.80")
        BigDecimal total,

        @Schema(description = "Data/hora em que o pedido foi criado (ISO-8601 UTC)",
                example = "2025-08-31T12:30:00Z")
        OffsetDateTime criadoEm,

        @Schema(description = "Data/hora em que o pedido foi pago (null se ainda não pago)",
                example = "2025-09-01T10:15:00Z")
        OffsetDateTime pagoEm,

        @Schema(description = "Data/hora em que o pedido foi cancelado (null se não cancelado)",
                example = "2025-09-01T18:00:00Z")
        OffsetDateTime canceladoEm,

        @Schema(description = "Lista de itens que compõem o pedido")
        List<PedidoItemResponse> itens
) {
}
