package br.com.mini.erp.repository;

import br.com.mini.erp.model.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    boolean existsBySku(String sku);

    Page<Produto> findByAtivo(Boolean ativo, Pageable pageable);

}
