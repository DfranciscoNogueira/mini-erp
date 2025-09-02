package br.com.mini.erp.controller;

import br.com.mini.erp.dto.request.PedidoRequest;
import br.com.mini.erp.dto.response.PedidoResponse;
import br.com.mini.erp.enuns.PedidoStatus;
import br.com.mini.erp.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Pedidos", description = "Gerenciamento de pedidos (criação, consulta, pagamento e cancelamento)")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @Operation(
            summary = "Cria um novo pedido",
            description = "Registra um novo pedido com cliente e itens. Estoque é atualizado e totais calculados.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do pedido a ser criado",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PedidoRequest.class),
                            examples = @ExampleObject(
                                    name = "Pedido exemplo",
                                    value = "{ \"clienteId\": 1, \"itens\": [ { \"produtoId\": 10, \"quantidade\": 2, \"desconto\": 5.00 } ] }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pedido criado com sucesso",
                            content = @Content(schema = @Schema(implementation = PedidoResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Cliente ou produto não encontrado", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Estoque insuficiente", content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<PedidoResponse> create(@Valid @RequestBody PedidoRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @Operation(
            summary = "Busca pedido por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pedido encontrado",
                            content = @Content(schema = @Schema(implementation = PedidoResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Pedido não encontrado", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> get(
            @Parameter(description = "ID do pedido", example = "1001")
            @PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @Operation(
            summary = "Lista pedidos com paginação",
            description = "Retorna lista paginada de pedidos. Pode ser filtrado por status.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de pedidos retornada com sucesso")
            }
    )
    @GetMapping
    public ResponseEntity<Page<PedidoResponse>> list(
            @Parameter(description = "Filtro de status do pedido", example = "CRIADO")
            @RequestParam(required = false) PedidoStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.list(status, pageable));
    }

    @Operation(
            summary = "Efetua o pagamento de um pedido",
            description = "Altera o status de um pedido para PAGO, se não estiver cancelado ou já pago.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pedido pago com sucesso",
                            content = @Content(schema = @Schema(implementation = PedidoResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Pedido já cancelado ou pago", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Pedido não encontrado", content = @Content)
            }
    )
    @PostMapping("/{id}/pay")
    public ResponseEntity<PedidoResponse> pay(
            @Parameter(description = "ID do pedido a ser pago", example = "1001")
            @PathVariable Long id) {
        return ResponseEntity.ok(service.pay(id));
    }

    @Operation(
            summary = "Cancela um pedido",
            description = "Cancela um pedido ainda não pago. Estoque dos itens é devolvido.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pedido cancelado com sucesso",
                            content = @Content(schema = @Schema(implementation = PedidoResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Pedido já pago, não pode ser cancelado", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Pedido não encontrado", content = @Content)
            }
    )
    @PostMapping("/{id}/cancel")
    public ResponseEntity<PedidoResponse> cancel(
            @Parameter(description = "ID do pedido a ser cancelado", example = "1001")
            @PathVariable Long id) {
        return ResponseEntity.ok(service.cancel(id));
    }

}
