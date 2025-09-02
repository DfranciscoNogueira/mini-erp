package br.com.mini.erp.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "EnderecoResponse", description = "Dados de endereço retornados para um cliente")
public record EnderecoResponse(

        @Schema(description = "Logradouro (rua, avenida, etc.)",
                example = "Praça da Sé")
        String logradouro,

        @Schema(description = "Número do endereço",
                example = "123")
        String numero,

        @Schema(description = "Complemento do endereço",
                example = "Apto 45")
        String complemento,

        @Schema(description = "Bairro",
                example = "Sé")
        String bairro,

        @Schema(description = "Cidade",
                example = "São Paulo")
        String cidade,

        @Schema(description = "Unidade Federativa (UF)",
                example = "SP")
        String uf,

        @Schema(description = "CEP",
                example = "01001-000")
        String cep
) {
}
