package br.com.mini.erp.client;

public record ViaCepResponse(
        String logradouro,
        String bairro,
        String localidade, // cidade
        String uf,
        String cep,
        Boolean erro
) {
}
