package br.com.mini.erp.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "ClienteRequest", description = "Requisição para criação ou atualização de cliente")
public record ClienteRequest(

        @Schema(description = "Nome completo do cliente",
                example = "João da Silva",
                maxLength = 150,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 150)
        String nome,

        @Schema(description = "E-mail único do cliente",
                example = "joao.silva@example.com",
                maxLength = 150,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Email
        @Size(max = 150)
        String email,

        @Schema(description = "CPF único do cliente (somente números, 11 dígitos)",
                example = "12345678900",
                maxLength = 14,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 14)
        String cpf,

        @Schema(description = "Endereço do cliente (será enriquecido automaticamente pelo CEP via ViaCEP)")
        EnderecoRequest endereco
) { }

