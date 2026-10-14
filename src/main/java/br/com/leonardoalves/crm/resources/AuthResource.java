package br.com.leonardoalves.crm.resources;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.leonardoalves.crm.dto.auth.AuthLoginDTO;
import br.com.leonardoalves.crm.dto.auth.AuthResponseDTO;
import br.com.leonardoalves.crm.services.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthResource {
	
	private final AuthService service;
	
	public AuthResource(AuthService service) {
		this.service = service;
	}
	
	@PostMapping("/login")
	public ResponseEntity<AuthResponseDTO> login(
	        @RequestBody AuthLoginDTO dto) {

	    System.out.println(">>> ENTROU NO AuthResource.login");

	    AuthResponseDTO response = service.login(dto);
	    return ResponseEntity.ok(response);
	}
}
