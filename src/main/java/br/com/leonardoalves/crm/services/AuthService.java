package br.com.leonardoalves.crm.services;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.leonardoalves.crm.domain.User;
import br.com.leonardoalves.crm.dto.auth.AuthLoginDTO;
import br.com.leonardoalves.crm.dto.auth.AuthResponseDTO;
import br.com.leonardoalves.crm.repositories.UserRepository;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtEncoder jwtEncoder;
	private final UserRepository repo;
	private final String issuer;
	private final String audience;
	private final java.time.Duration accessTtl;

	public AuthService(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder,
			UserRepository repo,
			@Value("${crm.jwt.issuer}") String issuer,
			@Value("${crm.jwt.audience}") String audience,
			@Value("${crm.jwt.access-ttl}") java.time.Duration accessTtl) {
		this.authenticationManager = authenticationManager;
		this.jwtEncoder = jwtEncoder;
		this.repo = repo;
		this.issuer = issuer;
		this.audience = audience;
		this.accessTtl = accessTtl;
	}

	@Transactional(readOnly = true)
	public AuthResponseDTO login(AuthLoginDTO dto) {
		// Senha errada ou conta desativada: isto LANCA, e o
		// GlobalExceptionHandler traduz em 401/403.
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(dto.email(), dto.password()));

		// Se o authenticate() passou, o usuario EXISTE. Chegar aqui e bug interno
		// (500), nao erro do cliente (401) — por isso IllegalStateException.
		User user = repo.findByEmail(dto.email())
				.orElseThrow(() -> new IllegalStateException(
						"Autenticado mas nao encontrado: " + dto.email()));

		Instant now = Instant.now();

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(issuer)
				.audience(List.of(audience))
				// O ID, nao o e-mail: e-mail muda, id nao.
				.subject(user.getId().toString())
				.issuedAt(now)
				.expiresAt(now.plus(accessTtl))
				.id(UUID.randomUUID().toString()) // jti
				.claim("uid", user.getId().toString())
				.claim("email", user.getEmail())
				.claim("name", user.getName())
				// SEM esta claim, @PreAuthorize e hasRole() nao tem como funcionar.
				.claim("roles", List.of("ROLE_" + user.getGlobalRole().name()))
				.build();

		String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

		return new AuthResponseDTO(token, "Bearer", accessTtl.toSeconds());
	}
}
