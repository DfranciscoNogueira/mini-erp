package br.com.mini.erp.service.impl;

import br.com.mini.erp.client.ViaCepClient;
import br.com.mini.erp.client.ViaCepResponse;
import br.com.mini.erp.dto.request.ClienteRequest;
import br.com.mini.erp.dto.request.EnderecoRequest;
import br.com.mini.erp.dto.response.ClienteResponse;
import br.com.mini.erp.dto.response.EnderecoResponse;
import br.com.mini.erp.exception.BusinessException;
import br.com.mini.erp.exception.NotFoundException;
import br.com.mini.erp.model.Cliente;
import br.com.mini.erp.model.Endereco;
import br.com.mini.erp.repository.ClienteRepository;
import br.com.mini.erp.service.ClienteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repository;
    private final ViaCepClient viaCep;

    public ClienteServiceImpl(ClienteRepository repository, ViaCepClient viaCep) {
        this.repository = repository;
        this.viaCep = viaCep;
    }

    @Override
    @Transactional
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2.0))
    public ClienteResponse create(ClienteRequest req) {
        validarUnicidade(req.email(), req.cpf());
        Cliente c = new Cliente();
        c.setNome(req.nome());
        c.setEmail(req.email());
        c.setCpf(req.cpf());
        c.setEndereco(enriquecerEndereco(req.endereco()));
        repository.saveAndFlush(c);
        return toResponse(c);
    }

    @Override
    @Transactional
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2.0))
    public ClienteResponse update(Long id, ClienteRequest req) {

        Cliente c = repository.findById(id).orElseThrow(() -> new NotFoundException("Cliente não encontrado"));

        if (!c.getEmail().equals(req.email()) && repository.existsByEmail(req.email())) {
            throw new BusinessException("Email já cadastrado");
        }

        if (!c.getCpf().equals(req.cpf()) && repository.existsByCpf(req.cpf())) {
            throw new BusinessException("CPF já cadastrado");
        }

        c.setNome(req.nome());
        c.setEmail(req.email());
        c.setCpf(req.cpf());
        c.setEndereco(enriquecerEndereco(req.endereco()));
        return toResponse(c);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Cliente c = repository.findById(id).orElseThrow(() -> new NotFoundException("Cliente não encontrado"));
        repository.delete(c);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse get(Long id) {
        Cliente c = repository.findById(id).orElseThrow(() -> new NotFoundException("Cliente não encontrado"));
        return toResponse(c);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteResponse> search(String q, Pageable pageable) {
        q = (q == null) ? "" : q;
        return repository.findByNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, pageable).map(this::toResponse);
    }

    private void validarUnicidade(String email, String cpf) {
        if (repository.existsByEmail(email)) throw new BusinessException("Email já cadastrado");
        if (repository.existsByCpf(cpf)) throw new BusinessException("CPF já cadastrado");
    }

    private Endereco enriquecerEndereco(EnderecoRequest req) {

        if (req == null || req.cep() == null || req.cep().isBlank()) {
            throw new BusinessException("CEP é obrigatório");
        }


        ViaCepResponse v = viaCep.get(req.cep());
        if (Boolean.TRUE.equals(v.erro())) throw new BusinessException("CEP inválido");

        Endereco e = new Endereco();
        e.setCep(req.cep());
        e.setNumero(req.numero());
        e.setComplemento(req.complemento());

        e.setLogradouro(firstNonBlank(req.logradouro(), v.logradouro()));
        e.setBairro(firstNonBlank(req.bairro(), v.bairro()));
        e.setCidade(firstNonBlank(req.cidade(), v.localidade()));
        e.setUf(firstNonBlank(req.uf(), v.uf()));

        return e;
    }

    private String firstNonBlank(String a, String b) {
        return (a != null && !a.isBlank()) ? a : b;
    }

    private ClienteResponse toResponse(Cliente c) {
        Endereco e = c.getEndereco();
        EnderecoResponse er = new EnderecoResponse(
                e.getLogradouro(), e.getNumero(), e.getComplemento(),
                e.getBairro(), e.getCidade(), e.getUf(), e.getCep()
        );
        return new ClienteResponse(c.getId(), c.getNome(), c.getEmail(), c.getCpf(), er);
    }

}
