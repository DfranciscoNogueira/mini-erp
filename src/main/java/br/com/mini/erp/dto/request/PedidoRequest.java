package br.com.mini.erp.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(name = "PedidoRequest", description = "Requisição para criar um pedido com itens de produtos")
public record PedidoRequest(

        @Schema(description = "ID do cliente que está realizando o pedido", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Long clienteId,

        @ArraySchema(
                schema = @Schema(implementation = PedidoItemRequest.class),
                minItems = 1,
                arraySchema = @Schema(description = "Lista de itens do pedido (mínimo 1 item)")
        )
        @Size(min = 1)
        List<PedidoItemRequest> itens
) { }
