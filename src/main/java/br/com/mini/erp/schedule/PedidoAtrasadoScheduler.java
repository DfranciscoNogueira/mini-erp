package br.com.mini.erp.schedule;

import br.com.mini.erp.service.PedidoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PedidoAtrasadoScheduler {

    private static final Logger log = LoggerFactory.getLogger(PedidoAtrasadoScheduler.class);
    private final PedidoService pedidoService;

    public PedidoAtrasadoScheduler(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // A cada hora
    @Scheduled(cron = "0 0 * * * *")
    public void run() {
        int count = pedidoService.markLateOrders();
        if (count > 0) {
            log.info("Pedidos marcados como LATE: {}", count);
        }
    }

}

