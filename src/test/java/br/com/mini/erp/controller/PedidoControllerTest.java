package br.com.mini.erp.controller;

import br.com.mini.erp.dto.request.PedidoItemRequest;
import br.com.mini.erp.dto.request.PedidoRequest;
import br.com.mini.erp.dto.response.PedidoItemResponse;
import br.com.mini.erp.dto.response.PedidoResponse;
import br.com.mini.erp.enuns.PedidoStatus;
import br.com.mini.erp.exception.NotFoundException;
import br.com.mini.erp.service.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PedidoService service;

    @Test
    void createDeveRetornar200ComPedidoCriado() throws Exception {

        var req = new PedidoRequest(
                1L,
                List.of(new PedidoItemRequest(10L, 2, new BigDecimal("5.00")))
        );

        var itemResp = new PedidoItemResponse(
                10L, "SKU-001", "Camiseta Azul",
                2, new BigDecimal("59.90"), new BigDecimal("5.00"), new BigDecimal("114.80")
        );

        var resp = new PedidoResponse(
                1001L, 1L, PedidoStatus.CRIADO,
                new BigDecimal("119.80"), new BigDecimal("5.00"), new BigDecimal("114.80"),
                OffsetDateTime.parse("2025-08-31T12:30:00Z"), null, null,
                List.of(itemResp)
        );

        when(service.create(any(PedidoRequest.class))).thenReturn(resp);

        mvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1001))
                .andExpect(jsonPath("$.clienteId").value(1))
                .andExpect(jsonPath("$.status").value("CRIADO"))
                .andExpect(jsonPath("$.subtotal").value(119.80))
                .andExpect(jsonPath("$.itens", hasSize(1)));

        verify(service).create(any(PedidoRequest.class));
    }

    @Test
    void createDeveRetornar400QuandoValidacaoFalha() throws Exception {

        var reqInvalido = """
                  {"clienteId":1,"itens":[]}
                """;

        mvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqInvalido))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }

    @Test
    void getDeveRetornar200ComPedido() throws Exception {

        var resp = new PedidoResponse(
                7L, 1L, PedidoStatus.CRIADO,
                new BigDecimal("10.00"), BigDecimal.ZERO, new BigDecimal("10.00"),
                OffsetDateTime.parse("2025-08-31T12:30:00Z"), null, null,
                List.of()
        );

        when(service.get(7L)).thenReturn(resp);

        mvc.perform(get("/api/v1/orders/{id}", 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("CRIADO"));

        verify(service).get(7L);
    }

    @Test
    void getDeveRetornar404QuandoNaoExiste() throws Exception {

        when(service.get(99L)).thenThrow(new NotFoundException("Pedido não encontrado"));

        mvc.perform(get("/api/v1/orders/{id}", 99))
                .andExpect(status().isNotFound());

        verify(service).get(99L);
    }

    @Test
    void listDeveRetornarPaginaFiltradaPorStatus() throws Exception {

        var pageResp = new PageImpl<>(
                List.of(new PedidoResponse(
                        1L, 1L, PedidoStatus.CRIADO,
                        new BigDecimal("100.00"), BigDecimal.ZERO, new BigDecimal("100.00"),
                        OffsetDateTime.parse("2025-08-31T12:00:00Z"), null, null,
                        List.of()
                )),
                PageRequest.of(0, 2),
                1
        );

        when(service.list(eq(PedidoStatus.CRIADO), any(Pageable.class))).thenReturn(pageResp);

        mvc.perform(get("/api/v1/orders")
                        .param("status", "CRIADO")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("CRIADO"))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(service).list(eq(PedidoStatus.CRIADO), any(Pageable.class));
    }

    @Test
    void payDeveRetornar200ComPedidoPago() throws Exception {

        var resp = new PedidoResponse(
                10L, 1L, PedidoStatus.PAGO,
                new BigDecimal("100.00"), BigDecimal.ZERO, new BigDecimal("100.00"),
                OffsetDateTime.parse("2025-08-31T12:00:00Z"),
                OffsetDateTime.parse("2025-09-01T10:00:00Z"),
                null,
                List.of()
        );

        when(service.pay(10L)).thenReturn(resp);

        mvc.perform(post("/api/v1/orders/{id}/pay", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAGO"))
                .andExpect(jsonPath("$.pagoEm").value("2025-09-01T10:00:00Z"));

        verify(service).pay(10L);
    }

    @Test
    void cancelDeveRetornar200ComPedidoCancelado() throws Exception {

        var resp = new PedidoResponse(
                11L, 1L, PedidoStatus.CANCELADO,
                new BigDecimal("100.00"), BigDecimal.ZERO, new BigDecimal("100.00"),
                OffsetDateTime.parse("2025-08-31T12:00:00Z"),
                null,
                OffsetDateTime.parse("2025-09-01T18:00:00Z"),
                List.of()
        );

        when(service.cancel(11L)).thenReturn(resp);

        mvc.perform(post("/api/v1/orders/{id}/cancel", 11))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"))
                .andExpect(jsonPath("$.canceladoEm").value("2025-09-01T18:00:00Z"));

        verify(service).cancel(11L);

    }

}
