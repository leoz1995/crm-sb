package br.com.leonardoalves.crm.services;

import org.springframework.stereotype.Service;

import br.com.leonardoalves.crm.domain.enterprise.Enterprise;
import br.com.leonardoalves.crm.repositories.EnterpriseRepository;

@Service
public class EnterpriseService {

	private final EnterpriseRepository repository;
	
	public EnterpriseService(EnterpriseRepository repository){
		this.repository = repository;
	}
	
	public Enterprise create(Enterprise enterprise) {
		if(enterprise.getCnpj() == null || enterprise.getCnpj().length() != 14) {
			throw new IllegalArgumentException("O CNPJ deve conter 14 digitos.");
		}
		
		if(enterprise.getName() == null) {
			throw new IllegalArgumentException("A empresa precisa ter um nome.");
		}
		
		String cnpj = enterprise.getCnpj();
		if(repository.existsByCnpj(cnpj)) {
			throw new IllegalArgumentException("Já existe uma empresa cadastrada com o CNPJ: "+cnpj+".");
		}
		
		enterprise.setName(enterprise.getName().trim());
		enterprise.setEmail(enterprise.getEmail() == null ? null : enterprise.getEmail().trim().toLowerCase());
		
		return repository.save(enterprise);
	}
}
