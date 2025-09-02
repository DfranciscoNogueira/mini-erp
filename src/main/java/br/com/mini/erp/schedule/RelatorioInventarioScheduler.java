package br.com.mini.erp.schedule;

import br.com.mini.erp.repository.ProdutoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RelatorioInventarioScheduler {
    private static final Logger log = LoggerFactory.getLogger(RelatorioInventarioScheduler.class);
    private final ProdutoRepository repository;

    public RelatorioInventarioScheduler(ProdutoRepository repository) {
        this.repository = repository;
    }

    // Diariamente às 03:00
    @Scheduled(cron = "0 0 3 * * *")
    public void run() {
        repository.findAll().stream()
                .filter(p -> p.getEstoque() < p.getEstoqueMinimo())
                .forEach(p -> log.warn("Reabastecimento necessário: SKU={}, estoque={}, min={}", p.getSku(), p.getEstoque(), p.getEstoqueMinimo()));
    }

}
