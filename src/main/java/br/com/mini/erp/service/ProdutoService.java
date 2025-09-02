package br.com.mini.erp.service;

import br.com.mini.erp.dto.request.ProdutoRequest;
import br.com.mini.erp.dto.response.ProdutoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProdutoService {

    ProdutoResponse create(ProdutoRequest req);

    ProdutoResponse update(Long id, ProdutoRequest req);

    void delete(Long id);

    ProdutoResponse get(Long id);

    Page<ProdutoResponse> list(Boolean ativo, Pageable pageable);

}
