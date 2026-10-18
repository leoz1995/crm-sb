package br.com.leonardoalves.crm.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.leonardoalves.crm.dto.auth.AuthLoginDTO;
import br.com.leonardoalves.crm.dto.auth.AuthResponseDTO;
import br.com.leonardoalves.crm.services.AuthService;
import jakarta.validation.Valid;

@RestController // = @Controller + @ResponseBody em todo método
@RequestMapping("/api/v1/auth")
public class AuthResource {

	private static final Logger log = LoggerFactory.getLogger(AuthResource.class);

	private final AuthService service;

	public AuthResource(AuthService service) {
		this.service = service;
	}

	@PostMapping("/login")
	public AuthResponseDTO login(@Valid @RequestBody AuthLoginDTO dto) {
		// Nunca logar e-mail completo nem a senha: log vira vazamento de PII
		// e de credencial (vai para arquivo, docker logs e agregador).
		log.debug("Tentativa de login");
		return service.login(dto);
	}
}
