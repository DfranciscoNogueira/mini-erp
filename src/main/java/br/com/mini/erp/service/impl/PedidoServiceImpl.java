package br.com.mini.erp.service.impl;

import br.com.mini.erp.dto.request.PedidoItemRequest;
import br.com.mini.erp.dto.request.PedidoRequest;
import br.com.mini.erp.dto.response.PedidoItemResponse;
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
import br.com.mini.erp.service.PedidoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoServiceImpl implements PedidoService {

    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;

    public PedidoServiceImpl(PedidoRepository pedidoRepository, ClienteRepository clienteRepository, ProdutoRepository produtoRepository) {
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    @Transactional
    public PedidoResponse create(PedidoRequest req) {

        Cliente cliente = clienteRepository.findById(req.clienteId()).orElseThrow(() -> new NotFoundException("Cliente não encontrado"));

        if (req.itens() == null || req.itens().isEmpty()) {
            throw new BusinessException("Pedido deve conter ao menos um item");
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal descontos = BigDecimal.ZERO;

        for (PedidoItemRequest it : req.itens()) {

            Produto prod = produtoRepository.findById(it.produtoId()).orElseThrow(() -> new NotFoundException("Produto " + it.produtoId() + " não encontrado"));

            if (prod.getEstoque() < it.quantidade()) {
                throw new BusinessException("Estoque insuficiente para SKU " + prod.getSku());
            }

            prod.setEstoque(prod.getEstoque() - it.quantidade());

            BigDecimal precoUnit = prod.getPrecoBruto();
            BigDecimal desconto = it.desconto() == null ? BigDecimal.ZERO : it.desconto();
            BigDecimal totalLinha = precoUnit.multiply(BigDecimal.valueOf(it.quantidade())).subtract(desconto);

            totalLinha = totalLinha.setScale(2, RoundingMode.HALF_UP);

            PedidoItem item = new PedidoItem();
            item.setProduto(prod);
            item.setQuantidade(it.quantidade());
            item.setPrecoUnitario(precoUnit.setScale(2, RoundingMode.HALF_UP));
            item.setDesconto(desconto.setScale(2, RoundingMode.HALF_UP));
            item.setTotalLinha(totalLinha);

            pedido.addItem(item);

            subtotal = subtotal.add(precoUnit.multiply(BigDecimal.valueOf(it.quantidade())));
            descontos = descontos.add(desconto);
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        descontos = descontos.setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.subtract(descontos).setScale(2, RoundingMode.HALF_UP);

        pedido.setSubtotal(subtotal);
        pedido.setDescontos(descontos);
        pedido.setTotal(total);

        pedidoRepository.saveAndFlush(pedido);
        return toResponse(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponse get(Long id) {
        Pedido p = pedidoRepository.findById(id).orElseThrow(() -> new NotFoundException("Pedido não encontrado"));
        return toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoResponse> list(PedidoStatus status, Pageable pageable) {
        if (status == null) {
            return pedidoRepository.findAll(pageable).map(this::toResponse);
        }
        return pedidoRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public PedidoResponse pay(Long id) {
        Pedido p = pedidoRepository.findById(id).orElseThrow(() -> new NotFoundException("Pedido não encontrado"));
        if (p.getStatus() == PedidoStatus.CANCELADO) throw new BusinessException("Pedido cancelado");
        if (p.getStatus() == PedidoStatus.PAGO) throw new BusinessException("Pedido já pago");
        p.setStatus(PedidoStatus.PAGO);
        p.setPagoEm(OffsetDateTime.now());
        return toResponse(p);
    }

    @Override
    @Transactional
    public PedidoResponse cancel(Long id) {

        Pedido p = pedidoRepository.findById(id).orElseThrow(() -> new NotFoundException("Pedido não encontrado"));
        if (p.getStatus() == PedidoStatus.PAGO) throw new BusinessException("Pedido já pago; não pode cancelar");
        if (p.getStatus() == PedidoStatus.CANCELADO) return toResponse(p);

        p.getItens().forEach(it -> {
            Produto prod = it.getProduto();
            prod.setEstoque(prod.getEstoque() + it.getQuantidade());
        });

        p.setStatus(PedidoStatus.CANCELADO);
        p.setCanceladoEm(OffsetDateTime.now());

        return toResponse(p);
    }

    @Override
    @Transactional
    public int markLateOrders() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusHours(48);
        List<Pedido> affected = new ArrayList<>(pedidoRepository.findByStatusAndCriadoEmBefore(PedidoStatus.CRIADO, cutoff));
        affected.forEach(p -> p.setStatus(PedidoStatus.ATRASADO));
        return affected.size();
    }

    private PedidoResponse toResponse(Pedido p) {
        List<PedidoItemResponse> itens = p.getItens().stream().map(it ->
                new PedidoItemResponse(
                        it.getProduto().getId(),
                        it.getProduto().getSku(),
                        it.getProduto().getNome(),
                        it.getQuantidade(),
                        it.getPrecoUnitario(),
                        it.getDesconto(),
                        it.getTotalLinha()
                )).toList();

        return new PedidoResponse(
                p.getId(),
                p.getCliente().getId(),
                p.getStatus(),
                p.getSubtotal(),
                p.getDescontos(),
                p.getTotal(),
                p.getCriadoEm(),
                p.getPagoEm(),
                p.getCanceladoEm(),
                itens
        );
    }
}
