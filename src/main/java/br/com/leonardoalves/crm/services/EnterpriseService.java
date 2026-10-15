package br.com.leonardoalves.crm.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.leonardoalves.crm.domain.User;
import br.com.leonardoalves.crm.domain.enterprise.Enterprise;
import br.com.leonardoalves.crm.domain.enterprise.EnterpriseMembership;
import br.com.leonardoalves.crm.domain.enums.EnterpriseRole;
import br.com.leonardoalves.crm.dto.EnterpriseCreateDTO;
import br.com.leonardoalves.crm.repositories.EnterpriseMembershipRepository;
import br.com.leonardoalves.crm.repositories.EnterpriseRepository;
import br.com.leonardoalves.crm.repositories.UserRepository;
import br.com.leonardoalves.crm.services.exception.ObjectNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnterpriseService {

	private final EnterpriseRepository repository;
	private final EnterpriseMembershipRepository membershipRepository;
	private final UserRepository userRepository;
	
	public EnterpriseService(EnterpriseRepository repository, EnterpriseMembershipRepository membershipRepository, UserRepository userRepository){
		this.repository = repository;
		this.membershipRepository = membershipRepository;
		this.userRepository = userRepository;
	}
	
	@Transactional
	public Enterprise create(Enterprise enterprise, String authenticatedEmail) {
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
		
		User user = userRepository.findByEmail(authenticatedEmail.trim().toLowerCase())
				.orElseThrow(()-> new ObjectNotFoundException("Usuário auenticado não encontrado."));
		
		enterprise.setName(enterprise.getName().trim());
		enterprise.setCnpj(enterprise.getCnpj().trim());
		enterprise.setEmail(enterprise.getEmail() == null ? null : enterprise.getEmail().trim().toLowerCase());
		enterprise.setPhone(enterprise.getPhone());
		enterprise.setZipCode(enterprise.getZipCode());
		enterprise.setStreet(enterprise.getStreet());
		enterprise.setComplement(normalizeOptional(enterprise.getComplement()));
		enterprise.setNeighborhood(normalizeOptional(enterprise.getNeighborhood()));
		enterprise.setCity(normalizeOptional(enterprise.getCity()));
		enterprise.setState(normalizeOptional(enterprise.getState())); // caixa alta.
		
		Enterprise savedEnterprise = repository.save(enterprise);
		EnterpriseMembership membership = new EnterpriseMembership(user, savedEnterprise, EnterpriseRole.OWNER);
		
		membershipRepository.save(membership);
		
		return savedEnterprise;
	}
	
	@Transactional
	public List<Enterprise> findALlByAuthenticatedUser(String authenticatedEmail){
		return repository.findAllAccessibleByUserEmail(normalizeEmail(authenticatedEmail));
	}
	
	@Transactional(readOnly=true)
	public Enterprise findByIdAndAuthenticatedUser(
	        UUID id,
	        String authenticatedEmail) {

	    return repository.findAccessibleByIdAndUserEmail(
	            id,
	            normalizeEmail(authenticatedEmail)
	    ).orElseThrow(() ->
	        new ObjectNotFoundException(
	            "Empresa não encontrada."
	        )
	    );
	}
	
	private String normalizeEmail(String email) {
	    return email.trim().toLowerCase(java.util.Locale.ROOT);
	}
	
	public Enterprise findById(UUID uuid) {
		Enterprise enterprise = repository.findById(uuid)
				.orElseThrow(() -> new ObjectNotFoundException("Objeto não encontrado."));
		
		return enterprise;
	}
	
	public void delete (UUID id) {
		findById(id);
		repository.deleteById(id);
	}
	
	public Enterprise fromDTO(EnterpriseCreateDTO enterpriseDTO) {
		return new Enterprise(enterpriseDTO.getName(), enterpriseDTO.getCnpj() ,enterpriseDTO.getEmail(),
				enterpriseDTO.getPhone(), enterpriseDTO.getZipCode(), enterpriseDTO.getStreet(), enterpriseDTO.getComplement(),
				enterpriseDTO.getNeighborhood(), enterpriseDTO.getCity(), enterpriseDTO.getState());
	}
	
	private String normalizeOptional(String value) {
		return value == null ? null : value.trim();
	}
}
