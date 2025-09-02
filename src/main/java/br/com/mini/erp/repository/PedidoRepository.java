package br.com.mini.erp.repository;

import br.com.mini.erp.enuns.PedidoStatus;
import br.com.mini.erp.model.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Page<Pedido> findByStatus(PedidoStatus status, Pageable pageable);

    List<Pedido> findByStatusAndCriadoEmBefore(PedidoStatus status, OffsetDateTime cutoff);

}
