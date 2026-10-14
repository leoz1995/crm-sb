package br.com.leonardoalves.crm.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import br.com.leonardoalves.crm.domain.User;
import br.com.leonardoalves.crm.dto.auth.AuthLoginDTO;
import br.com.leonardoalves.crm.dto.auth.AuthResponseDTO;
import br.com.leonardoalves.crm.repositories.UserRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
	
	private final AuthenticationManager authenticationManager;
	private final JwtEncoder jwtEncoder;
	private final UserRepository repo;
	
	public AuthService(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder, UserRepository repo) {
		this.authenticationManager = authenticationManager;
		this.jwtEncoder = jwtEncoder;
		this.repo = repo;
	}
	
    @Transactional(readOnly = true)
	public AuthResponseDTO login(AuthLoginDTO dto) {
		// UserService.create() grava o e-mail normalizado, entao a busca tambem normaliza.
		String email = dto.email().trim().toLowerCase();

		var authenticationToken = new UsernamePasswordAuthenticationToken(email, dto.password());
		
		authenticationManager.authenticate(authenticationToken);
		
		User user = repo.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
		
		Instant now = Instant.now();
		
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("crm-api")
				.subject(user.getEmail())
				.issuedAt(now)
				.expiresAt(now.plus(24, ChronoUnit.HOURS))
				.claim("email", user.getEmail())
				.build();
		
		String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
		
		return new AuthResponseDTO(token, "Bearer");
		
	}
}
