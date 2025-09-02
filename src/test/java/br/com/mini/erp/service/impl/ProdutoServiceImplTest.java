package br.com.mini.erp.service.impl;

import br.com.mini.erp.dto.request.ProdutoRequest;
import br.com.mini.erp.dto.response.ProdutoResponse;
import br.com.mini.erp.exception.BusinessException;
import br.com.mini.erp.exception.NotFoundException;
import br.com.mini.erp.model.Produto;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceImplTest {

    @Mock
    private ProdutoRepository repository;

    @InjectMocks
    private ProdutoServiceImpl service;

    private ProdutoRequest reqNovo;
    private Produto produtoDb;

    @BeforeEach
    void setup() {

        reqNovo = new ProdutoRequest(
                "SKU-001",
                "Camiseta Azul",
                new BigDecimal("59.90"),
                50,
                10,
                true
        );

        produtoDb = new Produto();
        produtoDb.setId(1L);
        produtoDb.setSku("SKU-001");
        produtoDb.setNome("Camiseta Azul");
        produtoDb.setPrecoBruto(new BigDecimal("59.90"));
        produtoDb.setEstoque(50);
        produtoDb.setEstoqueMinimo(10);
        produtoDb.setAtivo(true);
    }

    @Test
    void createDeveCriarQuandoSkuNaoExiste() {

        when(repository.existsBySku("SKU-001")).thenReturn(false);
        ArgumentCaptor<Produto> captor = ArgumentCaptor.forClass(Produto.class);
        when(repository.saveAndFlush(captor.capture())).thenAnswer(inv -> {
            Produto p = inv.getArgument(0);
            p.setId(99L);
            return p;
        });

        ProdutoResponse resp = service.create(reqNovo);

        Produto salvo = captor.getValue();
        assertEquals("SKU-001", salvo.getSku());
        assertEquals("Camiseta Azul", salvo.getNome());
        assertEquals(new BigDecimal("59.90"), salvo.getPrecoBruto());
        assertEquals(50, salvo.getEstoque());
        assertEquals(10, salvo.getEstoqueMinimo());
        assertTrue(salvo.getAtivo());

        assertEquals(99L, resp.id());
        assertEquals("SKU-001", resp.sku());
        verify(repository).saveAndFlush(any(Produto.class));
    }

    @Test
    void createDeveLancarQuandoSkuDuplicado() {
        when(repository.existsBySku("SKU-001")).thenReturn(true);
        assertThrows(BusinessException.class, () -> service.create(reqNovo));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void updateDeveAtualizarQuandoSkuNaoTrocaOuNaoConflita() {

        when(repository.findById(1L)).thenReturn(Optional.of(produtoDb));

        ProdutoRequest req = new ProdutoRequest(
                "SKU-001",
                "Camiseta Premium",
                new BigDecimal("79.90"),
                40,
                5,
                true
        );

        ProdutoResponse resp = service.update(1L, req);

        assertEquals("Camiseta Premium", resp.nome());
        assertEquals(new BigDecimal("79.90"), resp.precoBruto());
        assertEquals(40, resp.estoque());
        assertEquals(5, resp.estoqueMinimo());
        verify(repository, never()).existsBySku(anyString());
    }

    @Test
    void updateDeveLancarQuandoAlterarSkuParaDuplicado() {

        when(repository.findById(1L)).thenReturn(Optional.of(produtoDb));

        ProdutoRequest req = new ProdutoRequest(
                "SKU-002",
                "Outro",
                new BigDecimal("10.00"),
                1,
                0,
                true
        );
        when(repository.existsBySku("SKU-002")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.update(1L, req));
    }

    @Test
    void updateDeveLancarQuandoNaoEncontrado() {

        when(repository.findById(99L)).thenReturn(Optional.empty());

        ProdutoRequest req = new ProdutoRequest(
                "SKU-XYZ",
                "Qualquer",
                new BigDecimal("10.00"),
                1,
                0,
                true
        );

        assertThrows(NotFoundException.class, () -> service.update(99L, req));
    }

    @Test
    void deleteDeveRemoverQuandoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.of(produtoDb));
        service.delete(1L);
        verify(repository).delete(produtoDb);
    }

    @Test
    void deleteDeveLancarQuandoNaoExiste() {
        when(repository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.delete(2L));
        verify(repository, never()).delete(any());
    }

    @Test
    void getDeveRetornarQuandoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.of(produtoDb));
        ProdutoResponse resp = service.get(1L);
        assertEquals(1L, resp.id());
        assertEquals("SKU-001", resp.sku());
    }

    @Test
    void getDeveLancarQuandoNaoExiste() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.get(9L));
    }

    @Test
    void listSemFiltroAtivoDeveDelegarFindAll() {

        Page<Produto> page = new PageImpl<>(List.of(produtoDb));
        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        Page<ProdutoResponse> resp = service.list(null, PageRequest.of(0, 20));

        assertEquals(1, resp.getTotalElements());
        verify(repository).findAll(any(Pageable.class));
        verify(repository, never()).findByAtivo(anyBoolean(), any(Pageable.class));
    }

    @Test
    void listComAtivoTrueDeveDelegarFindByAtivo() {

        Page<Produto> page = new PageImpl<>(List.of(produtoDb));
        when(repository.findByAtivo(true, PageRequest.of(0, 10))).thenReturn(page);

        Page<ProdutoResponse> resp = service.list(true, PageRequest.of(0, 10));

        assertEquals(1, resp.getTotalElements());
        assertEquals("SKU-001", resp.getContent().get(0).sku());
        verify(repository).findByAtivo(true, PageRequest.of(0, 10));
    }

    @Test
    void listComAtivoFalseDeveDelegarFindByAtivo() {
        produtoDb.setAtivo(false);
        Page<Produto> page = new PageImpl<>(List.of(produtoDb));
        when(repository.findByAtivo(false, PageRequest.of(1, 5))).thenReturn(page);

        Page<ProdutoResponse> resp = service.list(false, PageRequest.of(1, 5));

        assertEquals(1, resp.getTotalElements());
        assertFalse(resp.getContent().get(0).ativo());
        verify(repository).findByAtivo(false, PageRequest.of(1, 5));
    }

}
