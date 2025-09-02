package br.com.mini.erp.service.impl;

import br.com.mini.erp.dto.request.ProdutoRequest;
import br.com.mini.erp.dto.response.ProdutoResponse;
import br.com.mini.erp.exception.BusinessException;
import br.com.mini.erp.exception.NotFoundException;
import br.com.mini.erp.model.Produto;
import br.com.mini.erp.repository.ProdutoRepository;
import br.com.mini.erp.service.ProdutoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoServiceImpl(ProdutoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ProdutoResponse create(ProdutoRequest req) {
        if (repository.existsBySku(req.sku())) {
            throw new BusinessException("SKU já cadastrado");
        }
        Produto p = toEntity(req, new Produto());
        repository.saveAndFlush(p);
        return toResponse(p);
    }

    @Override
    @Transactional
    public ProdutoResponse update(Long id, ProdutoRequest req) {
        Produto p = repository.findById(id).orElseThrow(() -> new NotFoundException("Produto não encontrado"));
        if (!p.getSku().equals(req.sku()) && repository.existsBySku(req.sku())) {
            throw new BusinessException("SKU já cadastrado");
        }
        toEntity(req, p);
        return toResponse(p);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Produto p = repository.findById(id).orElseThrow(() -> new NotFoundException("Produto não encontrado"));
        repository.delete(p);
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoResponse get(Long id) {
        Produto p = repository.findById(id).orElseThrow(() -> new NotFoundException("Produto não encontrado"));
        return toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProdutoResponse> list(Boolean ativo, Pageable pageable) {
        if (ativo == null) {
            return repository.findAll(pageable).map(this::toResponse);
        }
        return repository.findByAtivo(ativo, pageable).map(this::toResponse);
    }

    private Produto toEntity(ProdutoRequest req, Produto p) {
        p.setSku(req.sku());
        p.setNome(req.nome());
        p.setPrecoBruto(req.precoBruto());
        p.setEstoque(req.estoque());
        p.setEstoqueMinimo(req.estoqueMinimo());
        p.setAtivo(req.ativo());
        return p;
    }

    private ProdutoResponse toResponse(Produto p) {
        return new ProdutoResponse(p.getId(), p.getSku(), p.getNome(), p.getPrecoBruto(), p.getEstoque(), p.getEstoqueMinimo(), p.getAtivo());
    }

}
