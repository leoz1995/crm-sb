package br.com.leonardoalves.crm.resources;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.leonardoalves.crm.domain.enterprise.Enterprise;
import br.com.leonardoalves.crm.dto.EnterpriseCreateDTO;
import br.com.leonardoalves.crm.dto.EnterpriseDTO;
import br.com.leonardoalves.crm.services.EnterpriseService;

@RestController
@RequestMapping(value="/api/v1/enterprise")
public class EnterpriseResource {
	
	private final EnterpriseService service;
	
	EnterpriseResource(EnterpriseService service){
		this.service = service;
	}
	
	@PostMapping
	public ResponseEntity<Void> create(@RequestBody EnterpriseCreateDTO enterpriseDTO, Authentication authentication)
	{	
		Enterprise enterprise = service.fromDTO(enterpriseDTO);
		enterprise = service.create(enterprise, authentication.getName());
		
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(enterprise.getId()).toUri();
		
		return ResponseEntity.created(uri).build();
	}

	@GetMapping
	public ResponseEntity<List<EnterpriseDTO>> findAll(Authentication authentication){
		List<Enterprise> enterprises = service.findALlByAuthenticatedUser(authentication.getName());
		List<EnterpriseDTO> response = enterprises.stream().map(EnterpriseDTO::new).toList();
		return ResponseEntity.ok(response);
	}
	
	@RequestMapping(value="/{id}", method=RequestMethod.DELETE)
	public ResponseEntity<Void> delete(@PathVariable UUID id){	
		service.delete(id);
		return ResponseEntity.noContent().build(); //Retorna 204
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<EnterpriseDTO> findById(@PathVariable UUID id, Authentication authentication) {
	    Enterprise enterprise =
	        service.findByIdAndAuthenticatedUser(
	            id,
	            authentication.getName()
	        );

	    return ResponseEntity.ok(
	        new EnterpriseDTO(enterprise)
	    );
	}
}
