package br.com.leonardoalves.crm.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.leonardoalves.crm.services.EnterpriseService;

@Component
public class EnterpriseDeletionScheduler {

	private static final Logger log = LoggerFactory.getLogger(EnterpriseDeletionScheduler.class);
	
	private final EnterpriseService enterpriseService;
	
	public EnterpriseDeletionScheduler(EnterpriseService enterpriseService) {
		this.enterpriseService = enterpriseService;
	}
	
	@Scheduled(cron="0 0 3 * * *", zone="UTC")
	public void deleteExpiredEnterprises() {
		int deletedCount = enterpriseService.purgeExpiredDeletedEnterprises();
		if(deletedCount > 0) {
			log.info(
					"Limpeza concluída: {} empresa(s) removida(s) com sucesso!", deletedCount);
		}else
			log.info("Nenhuma empresa expirou o prazo de retenção.");
	}
}
