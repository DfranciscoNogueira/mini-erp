package br.com.mini.erp.service.impl;

import br.com.mini.erp.client.ViaCepClient;
import br.com.mini.erp.client.ViaCepResponse;
import br.com.mini.erp.dto.request.ClienteRequest;
import br.com.mini.erp.dto.request.EnderecoRequest;
import br.com.mini.erp.dto.response.ClienteResponse;
import br.com.mini.erp.exception.BusinessException;
import br.com.mini.erp.exception.NotFoundException;
import br.com.mini.erp.model.Cliente;
import br.com.mini.erp.model.Endereco;
import br.com.mini.erp.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository repository;
    @Mock
    private ViaCepClient viaCep;

    @InjectMocks
    private ClienteServiceImpl service;

    private Cliente clienteExistente;

    @BeforeEach
    void setUp() {
        clienteExistente = new Cliente();
        clienteExistente.setId(1L);
        clienteExistente.setNome("João");
        clienteExistente.setEmail("joao@ex.com");
        clienteExistente.setCpf("12345678900");
        Endereco e = new Endereco();
        e.setCep("01001-000");
        e.setCidade("São Paulo");
        e.setUf("SP");
        clienteExistente.setEndereco(e);
    }

    @Test
    void createDeveEnriquecerEnderecoComViaCepQuandoCamposFaltam() {

        var req = new ClienteRequest(
                "Maria",
                "maria@ex.com",
                "98765432100",
                new EnderecoRequest(null, "100", null, null, null, null, "01001-000")
        );

        when(repository.existsByEmail("maria@ex.com")).thenReturn(false);
        when(repository.existsByCpf("98765432100")).thenReturn(false);
        when(viaCep.get("01001-000")).thenReturn(new ViaCepResponse(
                "Praça da Sé", "Sé", "São Paulo", "SP", "01001-000", false
        ));

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        when(repository.saveAndFlush(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse resp = service.create(req);

        Cliente salvo = captor.getValue();
        assertEquals("Maria", salvo.getNome());
        assertEquals("maria@ex.com", salvo.getEmail());
        assertEquals("98765432100", salvo.getCpf());
        assertNotNull(salvo.getEndereco());
        assertEquals("Praça da Sé", salvo.getEndereco().getLogradouro()); // enriquecido
        assertEquals("Sé", salvo.getEndereco().getBairro());
        assertEquals("São Paulo", salvo.getEndereco().getCidade());
        assertEquals("SP", salvo.getEndereco().getUf());
        assertEquals("100", salvo.getEndereco().getNumero()); // veio do request

        assertEquals("Maria", resp.nome());
        assertEquals("Praça da Sé", resp.endereco().logradouro());
        verify(repository).saveAndFlush(any(Cliente.class));
    }

    @Test
    void createDeveLancarQuandoEmailJaExiste() {

        var req = new ClienteRequest("X", "dup@ex.com", "11122233344", new EnderecoRequest(null, null, null, null, null, null, "01001-000"));

        when(repository.existsByEmail("dup@ex.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.create(req));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void createDeveLancarQuandoCpfJaExiste() {

        var req = new ClienteRequest("X", "novo@ex.com", "11122233344", new EnderecoRequest(null, null, null, null, null, null, "01001-000"));

        when(repository.existsByEmail("novo@ex.com")).thenReturn(false);
        when(repository.existsByCpf("11122233344")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.create(req));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void createDeveLancarQuandoCepVazioOuNulo() {

        var req1 = new ClienteRequest("X", "x@ex.com", "00000000000", null);
        var req2 = new ClienteRequest("X", "x@ex.com", "00000000000", new EnderecoRequest(null, null, null, null, null, null, ""));

        assertThrows(BusinessException.class, () -> service.create(req1));
        assertThrows(BusinessException.class, () -> service.create(req2));
    }

    @Test
    void createDeveLancarQuandoViaCepRetornaErro() {

        var req = new ClienteRequest("X", "x@ex.com", "00000000000", new EnderecoRequest(null, "10", null, null, null, null, "01001-000"));

        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.existsByCpf(anyString())).thenReturn(false);
        when(viaCep.get("01001-000")).thenReturn(new ViaCepResponse(
                null, null, null, null, "01001-000", true
        ));

        assertThrows(BusinessException.class, () -> service.create(req));
    }

    @Test
    void updateDeveAtualizarDadosEReenriquecerEndereco() {

        when(repository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(repository.existsByEmail("novo@ex.com")).thenReturn(false);
        when(viaCep.get("01001-000")).thenReturn(new ViaCepResponse(
                "Praça da Sé", "Sé", "São Paulo", "SP", "01001-000", false
        ));

        var req = new ClienteRequest(
                "João da Silva",
                "novo@ex.com",
                "12345678900",
                new EnderecoRequest(null, "200", null, null, null, null, "01001-000")
        );

        ClienteResponse resp = service.update(1L, req);

        assertEquals("João da Silva", resp.nome());
        assertEquals("novo@ex.com", resp.email());
        assertEquals("Praça da Sé", resp.endereco().logradouro());
        assertEquals("200", resp.endereco().numero());
        verify(repository, never()).saveAndFlush(any());

    }

    @Test
    void updateDeveLancarQuandoEmailNovoJaExiste() {
        when(repository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(repository.existsByEmail("dup@ex.com")).thenReturn(true);
        var req = new ClienteRequest("J", "dup@ex.com", "12345678900", new EnderecoRequest(null, null, null, null, null, null, "01001-000"));
        assertThrows(BusinessException.class, () -> service.update(1L, req));
    }

    @Test
    void updateDeveLancarQuandoCpfNovoJaExiste() {
        when(repository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(repository.existsByCpf("99988877766")).thenReturn(true);
        var req = new ClienteRequest("J", "novo@ex.com", "99988877766", new EnderecoRequest(null, null, null, null, null, null, "01001-000"));
        assertThrows(BusinessException.class, () -> service.update(1L, req));
    }

    @Test
    void updateDeveLancarQuandoClienteNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        var req = new ClienteRequest("J", "j@ex.com", "000", new EnderecoRequest(null, null, null, null, null, null, "01001-000"));
        assertThrows(NotFoundException.class, () -> service.update(99L, req));
    }

    @Test
    void deleteDeveRemoverQuandoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        service.delete(1L);
        verify(repository).delete(clienteExistente);
    }

    @Test
    void deleteDeveLancarQuandoNaoExiste() {
        when(repository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.delete(2L));
        verify(repository, never()).delete(any());
    }

    @Test
    void getDeveRetornarQuandoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        ClienteResponse resp = service.get(1L);
        assertEquals(1L, resp.id());
        assertEquals("João", resp.nome());
        assertEquals("joao@ex.com", resp.email());
    }

    @Test
    void getDeveLancarQuandoNaoExiste() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.get(9L));
    }

    @Test
    void search_devePesquisarPorNomeOuEmail_comPaginacao() {
        var pageable = PageRequest.of(0, 10, Sort.by("nome"));
        var page = new PageImpl<>(List.of(clienteExistente));
        when(repository.findByNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(eq("jo"), eq("jo"), eq(pageable))).thenReturn(page);

        var result = service.search("jo", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("João", result.getContent().get(0).nome());
    }

}
