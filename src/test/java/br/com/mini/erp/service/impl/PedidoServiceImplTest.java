package br.com.mini.erp.service.impl;

import br.com.mini.erp.dto.request.PedidoItemRequest;
import br.com.mini.erp.dto.request.PedidoRequest;
import br.com.mini.erp.dto.response.PedidoResponse;
import br.com.mini.erp.enuns.PedidoStatus;
import br.com.mini.erp.exception.BusinessException;
import br.com.mini.erp.exception.NotFoundException;
import br.com.mini.erp.model.Cliente;
import br.com.mini.erp.model.Pedido;
import br.com.mini.erp.model.PedidoItem;
import br.com.mini.erp.model.Produto;
import br.com.mini.erp.repository.ClienteRepository;
import br.com.mini.erp.repository.PedidoRepository;
import br.com.mini.erp.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private PedidoServiceImpl service;

    private Cliente cliente;
    private Produto produto;

    @BeforeEach
    void setup() {

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João");
        cliente.setEmail("joao@example.com");
        cliente.setCpf("12345678900");

        produto = new Produto();
        produto.setId(10L);
        produto.setSku("SKU-001");
        produto.setNome("Camiseta");
        produto.setPrecoBruto(new BigDecimal("59.90"));
        produto.setEstoque(50);
        produto.setEstoqueMinimo(10);
        produto.setAtivo(true);

    }

    @Test
    void createDeveCalcularTotaisBaixarEstoque() {

        PedidoRequest req = new PedidoRequest(1L, List.of(new PedidoItemRequest(10L, 2, new BigDecimal("5.00"))));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto));

        ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        when(pedidoRepository.saveAndFlush(pedidoCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponse resp = service.create(req);

        assertEquals(new BigDecimal("119.80"), resp.subtotal());
        assertEquals(new BigDecimal("5.00"), resp.descontos());
        assertEquals(new BigDecimal("114.80"), resp.total());

        assertEquals(48, produto.getEstoque());

        Pedido salvo = pedidoCaptor.getValue();
        assertEquals(1, salvo.getItens().size());
        PedidoItem item = salvo.getItens().get(0);
        assertEquals(new BigDecimal("59.90"), item.getPrecoUnitario());
        assertEquals(new BigDecimal("5.00"), item.getDesconto());
        assertEquals(new BigDecimal("114.80"), item.getTotalLinha());
        assertEquals(PedidoStatus.CRIADO, salvo.getStatus());
        verify(pedidoRepository).saveAndFlush(any(Pedido.class));

    }

    @Test
    void createDeveLancarQuandoClienteNaoExiste() {
        PedidoRequest req = new PedidoRequest(99L, singletonList(new PedidoItemRequest(10L, 1, BigDecimal.ZERO)));
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.create(req));
        verify(pedidoRepository, never()).saveAndFlush(any());
    }

    @Test
    void createDeveLancarQuandoProdutoNaoExiste() {
        PedidoRequest req = new PedidoRequest(1L, singletonList(new PedidoItemRequest(999L, 1, BigDecimal.ZERO)));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtoRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.create(req));
    }

    @Test
    void createDeveLancarQuandoEstoqueInsuficiente() {
        produto.setEstoque(1);
        PedidoRequest req = new PedidoRequest(1L, singletonList(new PedidoItemRequest(10L, 2, BigDecimal.ZERO)));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto));
        assertThrows(BusinessException.class, () -> service.create(req));
    }

    @Test
    void createDeveLancarQuandoSemItens() {
        PedidoRequest req = new PedidoRequest(1L, List.of());
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        assertThrows(BusinessException.class, () -> service.create(req));
    }

    @Test
    void getDeveRetornarPedido() {

        Pedido p = novoPedidoCriadoComItem(cliente, produto, 2, new BigDecimal("5.00"));
        p.setId(123L);
        when(pedidoRepository.findById(123L)).thenReturn(Optional.of(p));

        PedidoResponse resp = service.get(123L);

        assertEquals(123L, resp.id());
        assertEquals(PedidoStatus.CRIADO, resp.status());
        assertEquals(new BigDecimal("119.80"), resp.subtotal());
        assertEquals(new BigDecimal("114.80"), resp.total());

    }

    @Test
    void getDeveLancarQuandoNaoEncontrado() {
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.get(999L));
    }

    @Test
    void listPorStatusDeveDelegarParaRepositorio() {

        Pedido p = novoPedidoCriadoComItem(cliente, produto, 1, BigDecimal.ZERO);
        Page<Pedido> page = new PageImpl<>(List.of(p));
        when(pedidoRepository.findByStatus(eq(PedidoStatus.CRIADO), any())).thenReturn(page);

        Page<PedidoResponse> resp = service.list(PedidoStatus.CRIADO, PageRequest.of(0, 20));

        assertEquals(1, resp.getTotalElements());
        assertEquals(PedidoStatus.CRIADO, resp.getContent().get(0).status());

    }

    @Test
    void listSemStatusDeveListarTodos() {

        Pedido p = novoPedidoCriadoComItem(cliente, produto, 1, BigDecimal.ZERO);
        Page<Pedido> page = new PageImpl<>(List.of(p));
        when(pedidoRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<PedidoResponse> resp = service.list(null, PageRequest.of(0, 20));

        assertEquals(1, resp.getTotalElements());
        verify(pedidoRepository).findAll(any(Pageable.class));
        verify(pedidoRepository, never()).findByStatus(any(), any());
    }

    @Test
    void payDeveAlterarParaPago() {
        Pedido p = novoPedidoCriadoSemItens(cliente);
        p.setId(10L);
        p.setStatus(PedidoStatus.CRIADO);
        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(p));

        PedidoResponse resp = service.pay(10L);

        assertEquals(PedidoStatus.PAGO, resp.status());
        assertNotNull(resp.pagoEm());
    }

    @Test
    void payDeveFalharQuandoCancelado() {
        Pedido p = novoPedidoCriadoSemItens(cliente);
        p.setStatus(PedidoStatus.CANCELADO);
        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(p));
        assertThrows(BusinessException.class, () -> service.pay(10L));
    }

    @Test
    void payDeveFalharQuandoJaPago() {
        Pedido p = novoPedidoCriadoSemItens(cliente);
        p.setStatus(PedidoStatus.PAGO);
        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(p));
        assertThrows(BusinessException.class, () -> service.pay(10L));
    }

    @Test
    void cancelDeveDevolverEstoqueMarcarCancelado() {
        Produto pr = cloneProduto(produto);
        pr.setEstoque(3);

        Pedido p = novoPedidoCriadoComItem(cliente, pr, 2, BigDecimal.ZERO);
        p.setStatus(PedidoStatus.CRIADO);
        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(p));

        PedidoResponse resp = service.cancel(10L);

        assertEquals(PedidoStatus.CANCELADO, resp.status());
        assertEquals(5, pr.getEstoque());
        assertNotNull(resp.canceladoEm());
    }

    @Test
    void cancelDeveFalharQuandoPago() {
        Pedido p = novoPedidoCriadoSemItens(cliente);
        p.setStatus(PedidoStatus.PAGO);
        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(p));
        assertThrows(BusinessException.class, () -> service.cancel(10L));
    }

    @Test
    void cancelDeveSerIdempotenteQuandoJaCancelado() {
        Produto pr = cloneProduto(produto);
        pr.setEstoque(3);

        Pedido p = novoPedidoCriadoComItem(cliente, pr, 2, BigDecimal.ZERO);
        p.setStatus(PedidoStatus.CANCELADO);
        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(p));

        PedidoResponse resp = service.cancel(10L);

        assertEquals(PedidoStatus.CANCELADO, resp.status());
        assertEquals(3, pr.getEstoque());
    }

    @Test
    void markLateOrdersDeveMarcarCriadosComoAtrasadosERetornarQuantidade() {

        Pedido p1 = novoPedidoCriadoSemItens(cliente);
        p1.setStatus(PedidoStatus.CRIADO);
        p1.setCriadoEm(OffsetDateTime.now().minusHours(60));

        Pedido p2 = novoPedidoCriadoSemItens(cliente);
        p2.setStatus(PedidoStatus.CRIADO);
        p2.setCriadoEm(OffsetDateTime.now().minusHours(49));

        when(pedidoRepository.findByStatusAndCriadoEmBefore(eq(PedidoStatus.CRIADO), any())).thenReturn(List.of(p1, p2));

        int count = service.markLateOrders();

        assertEquals(2, count);
        assertEquals(PedidoStatus.ATRASADO, p1.getStatus());
        assertEquals(PedidoStatus.ATRASADO, p2.getStatus());

    }

    private Pedido novoPedidoCriadoSemItens(Cliente c) {
        Pedido p = new Pedido();
        p.setCliente(c);
        p.setStatus(PedidoStatus.CRIADO);
        p.setSubtotal(BigDecimal.ZERO);
        p.setDescontos(BigDecimal.ZERO);
        p.setTotal(BigDecimal.ZERO);
        p.setCriadoEm(OffsetDateTime.now());
        return p;
    }

    private Pedido novoPedidoCriadoComItem(Cliente c, Produto pr, int qtd, BigDecimal desc) {
        Pedido p = novoPedidoCriadoSemItens(c);

        PedidoItem item = new PedidoItem();
        item.setProduto(pr);
        item.setQuantidade(qtd);
        item.setPrecoUnitario(pr.getPrecoBruto());
        item.setDesconto(desc);
        item.setTotalLinha(pr.getPrecoBruto().multiply(new BigDecimal(qtd)).subtract(desc));

        p.addItem(item);
        p.setSubtotal(pr.getPrecoBruto().multiply(new BigDecimal(qtd)));
        p.setDescontos(desc);
        p.setTotal(p.getSubtotal().subtract(desc));
        return p;
    }

    private Produto cloneProduto(Produto original) {
        Produto p = new Produto();
        p.setId(original.getId());
        p.setSku(original.getSku());
        p.setNome(original.getNome());
        p.setPrecoBruto(original.getPrecoBruto());
        p.setEstoque(original.getEstoque());
        p.setEstoqueMinimo(original.getEstoqueMinimo());
        p.setAtivo(original.getAtivo());
        return p;
    }

}
