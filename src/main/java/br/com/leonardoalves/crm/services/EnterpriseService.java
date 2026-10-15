package br.com.leonardoalves.crm.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.leonardoalves.crm.domain.User;
import br.com.leonardoalves.crm.domain.enterprise.Enterprise;
import br.com.leonardoalves.crm.domain.enterprise.EnterpriseMembership;
import br.com.leonardoalves.crm.domain.enums.EnterpriseRole;
import br.com.leonardoalves.crm.dto.EnterpriseCreateDTO;
import br.com.leonardoalves.crm.repositories.EnterpriseEmployeeRepository;
import br.com.leonardoalves.crm.repositories.EnterpriseMembershipRepository;
import br.com.leonardoalves.crm.repositories.EnterpriseRepository;
import br.com.leonardoalves.crm.repositories.UserRepository;
import br.com.leonardoalves.crm.services.exception.ObjectNotFoundException;

@Service
public class EnterpriseService {

	private final EnterpriseRepository repository;
	private final EnterpriseMembershipRepository membershipRepository;
	private final UserRepository userRepository;
	private final EnterpriseEmployeeRepository employeeRepository;
	
	public EnterpriseService(EnterpriseRepository repository, EnterpriseMembershipRepository membershipRepository
			, UserRepository userRepository, EnterpriseEmployeeRepository employeeRepository){
		this.repository = repository;
		this.membershipRepository = membershipRepository;
		this.userRepository = userRepository;
		this.employeeRepository = employeeRepository;
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
		enterprise.setEmail(normalizeEmail(enterprise.getEmail()));
		enterprise.setPhone(enterprise.getPhone());
		enterprise.setZipCode(enterprise.getZipCode());
		enterprise.setStreet(normalizeOptional(enterprise.getStreet()));
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
	
	@Transactional
	public void restoreDeletedEnterprise(UUID id) {
		Enterprise enterprise = repository.findById(id).orElseThrow(() ->
		new ObjectNotFoundException("Empresa não encontrada."));
		
		Instant deletedAt = enterprise.getDeletedAt();
		if(deletedAt == null) {
			throw new IllegalStateException("Esta empresa não está marcada para exclusão.");
		}
		
		Instant cutoff = Instant.now().minus(30, ChronoUnit.DAYS);
		
		if(!deletedAt.isAfter(cutoff)) {
			throw new IllegalStateException("O prazo de restauração de 30 dias expirou!");
		}
		
		enterprise.restore();
	}
	private String normalizeEmail(String email) {
	    return email == null ? null : email.trim().toLowerCase(java.util.Locale.ROOT);
	}
	
	public Enterprise findById(UUID uuid) {
		Enterprise enterprise = repository.findById(uuid)
				.orElseThrow(() -> new ObjectNotFoundException("Objeto não encontrado."));
		
		return enterprise;
	}
	
	@Transactional
	public void requestDeletion(UUID id, String authenticatedEmail) {
		String email = normalizeEmail(authenticatedEmail);

		Enterprise enterprise = repository.findAccessibleByIdAndUserEmail(id, email)
				.orElseThrow(() -> new ObjectNotFoundException("Empresa não encontrada."));

		checkOwner(id, email); // checkando owner

		enterprise.markAsDeleted();
	}
	
	@Transactional
	public int purgeExpiredDeletedEnterprises() {
		Instant cutoff = Instant.now().minus(30, ChronoUnit.DAYS);
		List<Enterprise> expiredEnterprises = repository.findAllByDeletedAtBefore(cutoff);
		
		for(Enterprise enterprise : expiredEnterprises) {
			UUID enterpriseId = enterprise.getId();
			
			// remove somente os vinculos da empresa.
			employeeRepository.deleteAllByEnterpriseId(enterpriseId);
			membershipRepository.deleteAllByEnterpriseId(enterpriseId);
			
			// remove a empresa totalmente.
			repository.delete(enterprise);
		}
		return expiredEnterprises.size();
	}
	
	public Enterprise fromDTO(EnterpriseCreateDTO enterpriseDTO) {
		return new Enterprise(enterpriseDTO.getName(), enterpriseDTO.getCnpj() ,enterpriseDTO.getEmail(),
				enterpriseDTO.getPhone(), enterpriseDTO.getZipCode(), enterpriseDTO.getStreet(), enterpriseDTO.getComplement(),
				enterpriseDTO.getNeighborhood(), enterpriseDTO.getCity(), enterpriseDTO.getState());
	}
	
	private void checkOwner(UUID id, String authenticatedEmail) {
		boolean isOwner = membershipRepository.existsByEnterpriseAndUserAndRole(id, authenticatedEmail, EnterpriseRole.OWNER);
		
		if(!isOwner) {
			throw new AccessDeniedException(
			"Somente o proprietário pode solicitar a exclusão.");
		}
	}
	private String normalizeOptional(String value) {
		return value == null ? null : value.trim();
	}
}
