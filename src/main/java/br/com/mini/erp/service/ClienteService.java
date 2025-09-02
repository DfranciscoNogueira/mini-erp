package br.com.mini.erp.service;

import br.com.mini.erp.dto.request.ClienteRequest;
import br.com.mini.erp.dto.response.ClienteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClienteService {

    ClienteResponse create(ClienteRequest req);

    ClienteResponse update(Long id, ClienteRequest req);

    void delete(Long id);

    ClienteResponse get(Long id);

    Page<ClienteResponse> search(String q, Pageable pageable);

}
