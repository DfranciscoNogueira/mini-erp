package br.com.mini.erp.service;

import br.com.mini.erp.dto.request.PedidoRequest;
import br.com.mini.erp.dto.response.PedidoResponse;
import br.com.mini.erp.enuns.PedidoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PedidoService {

    PedidoResponse create(PedidoRequest req);

    PedidoResponse get(Long id);

    Page<PedidoResponse> list(PedidoStatus status, Pageable pageable);

    PedidoResponse pay(Long id);

    PedidoResponse cancel(Long id);

    int markLateOrders(); // agendamento

}
