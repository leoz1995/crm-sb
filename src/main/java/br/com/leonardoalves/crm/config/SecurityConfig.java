package br.com.leonardoalves.crm.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // SEM ISTO, TODO @PreAuthorize E IGNORADO EM SILENCIO
public class SecurityConfig {

	private final String jwtSecret;
	private final String issuer;
	private final String audience;
	private final List<String> origensCors;

	public SecurityConfig(
			@Value("${crm.jwt.secret}") String jwtSecret,
			@Value("${crm.jwt.issuer}") String issuer,
			@Value("${crm.jwt.audience}") String audience,
			@Value("${crm.cors.origens}") List<String> origensCors) {
		this.jwtSecret = jwtSecret;
		this.issuer = issuer;
		this.audience = audience;
		this.origensCors = origensCors;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			// Bearer no header + STATELESS: nao ha credencial enviada automaticamente
			// pelo navegador, logo nao ha o que forjar. Reavaliar se adotar cookie.
			.csrf(csrf -> csrf.disable())

			.cors(Customizer.withDefaults())

			.sessionManagement(session -> session
					.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

			.headers(headers -> headers
					.frameOptions(fo -> fo.deny())
					.httpStrictTransportSecurity(hsts -> hsts
							.includeSubDomains(false)
							.maxAgeInSeconds(31_536_000))
					// API devolve so JSON: nao carrega script, estilo nem imagem.
					.contentSecurityPolicy(csp -> csp.policyDirectives(
							"default-src 'none'; frame-ancestors 'none'; base-uri 'none'"))
					.referrerPolicy(rp -> rp.policy(
							ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
					.permissionsPolicyHeader(pp -> pp.policy(
							"camera=(), microphone=(), geolocation=(), payment=()")))

			.authorizeHttpRequests(auth -> auth
					// O AuthorizationFilter tambem roda no dispatch de ERRO: sem liberar
					// /error, qualquer excecao volta como 401 de corpo vazio e esconde
					// a causa real.
					.requestMatchers("/error").permitAll()
					.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
					.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()

					// Cadastro de usuario e operacao administrativa: um CRM B2B usa
					// convite, nao auto-cadastro aberto na internet.
					.requestMatchers(HttpMethod.POST, "/api/v1/users")
							.hasRole("PLATFORM_OWNER")
					.requestMatchers(HttpMethod.GET, "/api/v1/users")
							.hasRole("PLATFORM_OWNER")
					.requestMatchers(HttpMethod.DELETE, "/api/v1/users/**")
							.hasRole("PLATFORM_OWNER")

					.anyRequest().authenticated())

			.oauth2ResourceServer(oauth2 -> oauth2
					.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

		return http.build();
	}

	/**
	 * AuthenticationManager EXPLICITO.
	 *
	 * Nao use AuthenticationConfiguration.getAuthenticationManager(): sem um
	 * UserDetailsService no contexto, authBuilder.build() devolve null e o
	 * fallback lazyBean(AuthenticationManager.class) resolve ESTE MESMO bean,
	 * gerando recursao infinita (StackOverflowError no login).
	 */
	@Bean
	AuthenticationManager authenticationManager(UserDetailsService uds, PasswordEncoder encoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(uds);
		provider.setPasswordEncoder(encoder);
		// UsernameNotFound -> BadCredentials: nao revela se o e-mail existe.
		provider.setHideUserNotFoundExceptions(true);
		return new ProviderManager(provider);
	}

	/**
	 * Converte a claim "roles" do token em GrantedAuthority.
	 *
	 * Sem isto, o resource server le apenas "scope"/"scp" e hasRole() NUNCA e
	 * verdade, mesmo com a claim roles presente no JWT.
	 */
	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
		authorities.setAuthoritiesClaimName("roles");
		authorities.setAuthorityPrefix(""); // o prefixo ROLE_ ja vem na claim

		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(authorities);
		converter.setPrincipalClaimName("uid");
		return converter;
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		// Nunca "*" junto com allowCredentials: o navegador recusa a combinacao.
		config.setAllowedOrigins(origensCors);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Idempotency-Key"));
		config.setExposedHeaders(List.of("Location", "Retry-After"));
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", config);
		return source;
	}

	@Bean
	SecretKey jwtSecretKey() {
		byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

		// HMAC-SHA256 trabalha com blocos de 256 bits: chave menor reduz a
		// entropia e viabiliza forca bruta offline sobre um token capturado.
		if (keyBytes.length < 32) {
			throw new IllegalStateException(
					"crm.jwt.secret precisa de no minimo 32 bytes para HS256; recebido: "
							+ keyBytes.length);
		}

		return new SecretKeySpec(keyBytes, "HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
		return NimbusJwtEncoder.withSecretKey(jwtSecretKey).algorithm(MacAlgorithm.HS256).build();
	}

	@Bean
	JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
				.macAlgorithm(MacAlgorithm.HS256)
				.build();

		// Sem isto o decoder valida assinatura e exp, mas NAO valida iss nem aud.
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
				new JwtTimestampValidator(Duration.ofSeconds(30)),
				new JwtIssuerValidator(issuer),
				new JwtClaimValidator<List<String>>(JwtClaimNames.AUD,
						aud -> aud != null && aud.contains(audience))));

		return decoder;
	}
}
