package br.com.leonardoalves.crm.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.leonardoalves.crm.domain.enterprise.Enterprise;

public interface EnterpriseRepository  extends JpaRepository<Enterprise, UUID>{

	boolean existsByCnpj(String cnpj);
	
	Optional<Enterprise> findByCnpj(String cnpj);
	
	boolean existsByEmail(String email);
}
