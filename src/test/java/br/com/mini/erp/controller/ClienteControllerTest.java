package br.com.mini.erp.controller;

import br.com.mini.erp.dto.request.ClienteRequest;
import br.com.mini.erp.dto.request.EnderecoRequest;
import br.com.mini.erp.dto.response.ClienteResponse;
import br.com.mini.erp.dto.response.EnderecoResponse;
import br.com.mini.erp.exception.NotFoundException;
import br.com.mini.erp.service.ClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ClienteService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createDeveRetornar200ComClienteCriado() throws Exception {

        var req = new ClienteRequest(
                "João da Silva",
                "joao@example.com",
                "12345678900",
                new EnderecoRequest(null, "100", null, null, null, null, "01001-000")
        );

        var resp = new ClienteResponse(
                1L, "João da Silva", "joao@example.com", "12345678900",
                new EnderecoResponse("Praça da Sé", "100", null, "Sé", "São Paulo", "SP", "01001-000")
        );

        when(service.create(any(ClienteRequest.class))).thenReturn(resp);

        mvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("João da Silva"))
                .andExpect(jsonPath("$.email").value("joao@example.com"))
                .andExpect(jsonPath("$.endereco.cep").value("01001-000"));

        verify(service).create(any(ClienteRequest.class));
    }

    @Test
    void createDeveRetornar400QuandoValidacaoFalha() throws Exception {

        var reqInvalido = """
                {"nome":"","email":"","cpf":"","endereco":{"cep":""}}
                """;

        mvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqInvalido))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }

    @Test
    void updateDeveRetornar200ComClienteAtualizado() throws Exception {

        var req = new ClienteRequest(
                "João Atualizado",
                "joao.atualizado@example.com",
                "12345678900",
                new EnderecoRequest(null, "200", null, null, null, null, "01001-000")
        );

        var resp = new ClienteResponse(
                1L, "João Atualizado", "joao.atualizado@example.com", "12345678900",
                new EnderecoResponse("Praça da Sé", "200", null, "Sé", "São Paulo", "SP", "01001-000")
        );

        when(service.update(eq(1L), any(ClienteRequest.class))).thenReturn(resp);

        mvc.perform(put("/api/v1/customers/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("João Atualizado"))
                .andExpect(jsonPath("$.endereco.numero").value("200"));

        verify(service).update(eq(1L), any(ClienteRequest.class));
    }

    @Test
    void updateDeveRetornar404QuandoNaoExiste() throws Exception {

        var req = new ClienteRequest(
                "X", "x@ex.com", "00000000000",
                new EnderecoRequest(null, "10", null, null, null, null, "01001-000")
        );

        when(service.update(eq(99L), any(ClienteRequest.class))).thenThrow(new NotFoundException("Cliente não encontrado"));

        mvc.perform(put("/api/v1/customers/{id}", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());

        verify(service).update(eq(99L), any(ClienteRequest.class));
    }

    @Test
    void getDeveRetornar200ComCliente() throws Exception {

        var resp = new ClienteResponse(
                42L, "Maria", "maria@example.com", "98765432100",
                new EnderecoResponse("Rua A", "123", null, "Centro", "RJ", "RJ", "20000-000")
        );

        when(service.get(42L)).thenReturn(resp);

        mvc.perform(get("/api/v1/customers/{id}", 42))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.email").value("maria@example.com"));

        verify(service).get(42L);
    }

    @Test
    void getDeveRetornar404QuandoNaoExiste() throws Exception {
        when(service.get(9L)).thenThrow(new NotFoundException("Cliente não encontrado"));
        mvc.perform(get("/api/v1/customers/{id}", 9))
                .andExpect(status().isNotFound());
        verify(service).get(9L);
    }

    @Test
    void delete_deveRetornar204() throws Exception {

        doNothing().when(service).delete(7L);

        mvc.perform(delete("/api/v1/customers/{id}", 7))
                .andExpect(status().isNoContent());

        verify(service).delete(7L);
    }

    @Test
    void searchDeveRetornarPagina() throws Exception {

        var item = new ClienteResponse(
                1L, "João", "joao@example.com", "12345678900",
                new EnderecoResponse("Rua X", "10", null, "Bairro", "Cidade", "UF", "00000-000")
        );

        Page<ClienteResponse> page = new PageImpl<>(List.of(item), PageRequest.of(0, 2), 1);

        when(service.search(eq("jo"), any(Pageable.class))).thenReturn(page);

        mvc.perform(get("/api/v1/customers")
                        .param("q", "jo")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("João"))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(service).search(eq("jo"), any(Pageable.class));
    }

}
