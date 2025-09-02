package br.com.mini.erp.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ClienteResponse", description = "Resposta com os dados de um cliente")
public record ClienteResponse(

        @Schema(description = "Identificador único do cliente",
                example = "1")
        Long id,

        @Schema(description = "Nome completo do cliente",
                example = "João da Silva")
        String nome,

        @Schema(description = "E-mail único do cliente",
                example = "joao.silva@example.com")
        String email,

        @Schema(description = "CPF único do cliente (somente números, 11 dígitos)",
                example = "12345678900")
        String cpf,

        @Schema(description = "Endereço do cliente")
        EnderecoResponse endereco
) {
}
