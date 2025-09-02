package br.com.mini.erp.controller;

import br.com.mini.erp.dto.request.ClienteRequest;
import br.com.mini.erp.dto.response.ClienteResponse;
import br.com.mini.erp.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Clientes", description = "Gerenciamento de clientes (CRUD e busca)")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @Operation(
            summary = "Cria um novo cliente",
            description = "Cadastra um cliente com nome, e-mail, CPF e endereço. O endereço é enriquecido automaticamente pelo CEP.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cliente criado com sucesso",
                            content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
                    @ApiResponse(responseCode = "409", description = "E-mail ou CPF já cadastrado", content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<ClienteResponse> create(@Valid @RequestBody ClienteRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @Operation(
            summary = "Atualiza um cliente existente",
            description = "Atualiza dados do cliente (nome, e-mail, CPF e endereço). O CEP é validado e pode atualizar endereço automaticamente.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso",
                            content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content),
                    @ApiResponse(responseCode = "409", description = "E-mail ou CPF já cadastrado", content = @Content)
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> update(
            @Parameter(description = "ID do cliente a ser atualizado", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @Operation(
            summary = "Busca cliente por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                            content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> get(
            @Parameter(description = "ID do cliente", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @Operation(
            summary = "Remove cliente por ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Cliente removido com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do cliente a ser removido", example = "1")
            @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Pesquisa clientes por nome ou e-mail",
            description = "Retorna lista paginada de clientes filtrados pelo parâmetro `q`. Caso `q` seja nulo, retorna todos os clientes.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de clientes retornada com sucesso")
            }
    )
    @GetMapping
    public ResponseEntity<Page<ClienteResponse>> search(
            @Parameter(description = "Filtro de pesquisa por nome ou e-mail", example = "joao")
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.search(q, pageable));
    }

}
