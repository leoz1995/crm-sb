package br.com.leonardoalves.crm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder(); // Hash com prefixo {hash}$2a$10$
//		return new Argon2PasswordEncoder(16, 32, 1, 60_000, 3); - Recomendação do OWASP
//		return new BCryptPasswordEncoder();
	}
}
