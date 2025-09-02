package br.com.mini.erp.controller;

import br.com.mini.erp.dto.request.ProdutoRequest;
import br.com.mini.erp.dto.response.ProdutoResponse;
import br.com.mini.erp.service.ProdutoService;
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
@RequestMapping("/api/v1/products")
@Tag(name = "Produtos", description = "Gerenciamento de produtos (CRUD e listagem)")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @Operation(
            summary = "Cria um novo produto",
            description = "Cadastra um produto com SKU, nome, preço, estoque e status ativo.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Produto criado com sucesso",
                            content = @Content(schema = @Schema(implementation = ProdutoResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
                    @ApiResponse(responseCode = "409", description = "SKU já cadastrado", content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<ProdutoResponse> create(@Valid @RequestBody ProdutoRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @Operation(
            summary = "Atualiza um produto existente",
            description = "Atualiza os dados de um produto identificado pelo ID. O SKU deve ser único.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso",
                            content = @Content(schema = @Schema(implementation = ProdutoResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content),
                    @ApiResponse(responseCode = "409", description = "SKU já cadastrado", content = @Content)
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponse> update(
            @Parameter(description = "ID do produto a ser atualizado", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ProdutoRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @Operation(
            summary = "Busca produto por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Produto encontrado",
                            content = @Content(schema = @Schema(implementation = ProdutoResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponse> get(
            @Parameter(description = "ID do produto", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @Operation(
            summary = "Remove produto por ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Produto removido com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do produto a ser removido", example = "1")
            @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Lista produtos com paginação",
            description = "Retorna lista paginada de produtos. É possível filtrar pelo status `ativo`.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso")
            }
    )
    @GetMapping
    public ResponseEntity<Page<ProdutoResponse>> list(
            @Parameter(description = "Filtro de status ativo do produto", example = "true")
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.list(ativo, pageable));
    }

}
