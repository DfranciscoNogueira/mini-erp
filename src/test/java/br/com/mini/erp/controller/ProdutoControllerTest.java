package br.com.mini.erp.controller;

import br.com.mini.erp.dto.request.ProdutoRequest;
import br.com.mini.erp.dto.response.ProdutoResponse;
import br.com.mini.erp.exception.NotFoundException;
import br.com.mini.erp.service.ProdutoService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProdutoController.class)
class ProdutoControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProdutoService service;

    @Test
    void createDeveRetornar200ComProdutoCriado() throws Exception {

        var req = new ProdutoRequest(
                "SKU-001", "Camiseta Azul",
                new BigDecimal("59.90"), 50, 10, true
        );

        var resp = new ProdutoResponse(
                1L, "SKU-001", "Camiseta Azul",
                new BigDecimal("59.90"), 50, 10, true
        );

        when(service.create(any(ProdutoRequest.class))).thenReturn(resp);

        mvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("SKU-001"))
                .andExpect(jsonPath("$.nome").value("Camiseta Azul"));

        verify(service).create(any(ProdutoRequest.class));
    }

    @Test
    void createDeveRetornar400QuandoValidacaoFalha() throws Exception {

        var reqInvalido = """
                  {"sku":"","nome":"","precoBruto":-1,"estoque":-1,"estoqueMinimo":-1,"ativo":null}
                """;

        mvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqInvalido))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }

    @Test
    void updateDeveRetornar200ComProdutoAtualizado() throws Exception {

        var req = new ProdutoRequest(
                "SKU-001", "Camiseta Premium",
                new BigDecimal("79.90"), 40, 5, true
        );

        var resp = new ProdutoResponse(
                1L, "SKU-001", "Camiseta Premium",
                new BigDecimal("79.90"), 40, 5, true
        );

        when(service.update(eq(1L), any(ProdutoRequest.class))).thenReturn(resp);

        mvc.perform(put("/api/v1/products/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Camiseta Premium"))
                .andExpect(jsonPath("$.precoBruto").value(79.90));

        verify(service).update(eq(1L), any(ProdutoRequest.class));
    }

    @Test
    void updateDeveRetornar404QuandoNaoExiste() throws Exception {

        var req = new ProdutoRequest(
                "SKU-XYZ", "Qualquer",
                new BigDecimal("10.00"), 1, 0, true
        );

        when(service.update(eq(99L), any(ProdutoRequest.class)))
                .thenThrow(new NotFoundException("Produto não encontrado"));

        mvc.perform(put("/api/v1/products/{id}", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());

        verify(service).update(eq(99L), any(ProdutoRequest.class));
    }

    @Test
    void getDeveRetornar200ComProduto() throws Exception {

        var resp = new ProdutoResponse(
                7L, "SKU-007", "Produto 7",
                new BigDecimal("10.00"), 5, 0, true
        );

        when(service.get(7L)).thenReturn(resp);

        mvc.perform(get("/api/v1/products/{id}", 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.sku").value("SKU-007"));

        verify(service).get(7L);
    }

    @Test
    void getDeveRetornar404QuandoNaoExiste() throws Exception {

        when(service.get(9L)).thenThrow(new NotFoundException("Produto não encontrado"));

        mvc.perform(get("/api/v1/products/{id}", 9))
                .andExpect(status().isNotFound());

        verify(service).get(9L);
    }

    @Test
    void deleteDeveRetornar204() throws Exception {

        doNothing().when(service).delete(5L);

        mvc.perform(delete("/api/v1/products/{id}", 5))
                .andExpect(status().isNoContent());

        verify(service).delete(5L);
    }

    @Test
    void deleteDeveRetornar404QuandoNaoExiste() throws Exception {

        doThrow(new NotFoundException("Produto não encontrado")).when(service).delete(66L);

        mvc.perform(delete("/api/v1/products/{id}", 66))
                .andExpect(status().isNotFound());

        verify(service).delete(66L);
    }

    @Test
    void listSemFiltroDeveRetornarPaginaCompleta() throws Exception {

        var item = new ProdutoResponse(
                1L, "SKU-001", "Camiseta",
                new BigDecimal("59.90"), 50, 10, true
        );

        Page<ProdutoResponse> page = new PageImpl<>(
                List.of(item), PageRequest.of(0, 2), 1
        );

        when(service.list(isNull(), any(Pageable.class))).thenReturn(page);

        mvc.perform(get("/api/v1/products")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].sku").value("SKU-001"))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(service).list(isNull(), any(Pageable.class));
    }

    @Test
    void listComFiltroAtivoTrueDeveRetornarApenasAtivos() throws Exception {

        var ativo = new ProdutoResponse(
                2L, "SKU-002", "Ativo",
                new BigDecimal("10.00"), 1, 0, true
        );

        Page<ProdutoResponse> page = new PageImpl<>(
                List.of(ativo), PageRequest.of(0, 10), 1
        );

        when(service.list(eq(true), any(Pageable.class))).thenReturn(page);

        mvc.perform(get("/api/v1/products")
                        .param("ativo", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].ativo").value(true))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(service).list(eq(true), any(Pageable.class));
    }

}
