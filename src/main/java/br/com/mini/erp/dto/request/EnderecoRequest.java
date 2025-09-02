package br.com.mini.erp.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "EnderecoRequest", description = "Dados de endereço do cliente. Caso logradouro/bairro/cidade/uf não sejam enviados, serão preenchidos automaticamente pelo CEP via ViaCEP.")
public record EnderecoRequest(

        @Schema(description = "Logradouro (rua, avenida, etc.)",
                example = "Praça da Sé",
                maxLength = 255)
        String logradouro,

        @Schema(description = "Número do endereço",
                example = "123")
        String numero,

        @Schema(description = "Complemento do endereço",
                example = "Apto 45")
        String complemento,

        @Schema(description = "Bairro",
                example = "Sé",
                maxLength = 150)
        String bairro,

        @Schema(description = "Cidade",
                example = "São Paulo",
                maxLength = 150)
        String cidade,

        @Schema(description = "Unidade Federativa (UF)",
                example = "SP",
                maxLength = 2)
        String uf,

        @Schema(description = "CEP (somente números ou formato 00000-000)",
                example = "01001-000",
                maxLength = 9,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 9)
        String cep
) {
}
