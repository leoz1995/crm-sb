package br.com.leonardoalves.crm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import br.com.leonardoalves.crm.repositories.UserRepository;

@Configuration
public class UserDetailsConfig {

	/*
	 * Sem este bean o AuthenticationManagerBuilder global fica sem nenhum
	 * provider: authBuilder.build() devolve null e o AuthenticationConfiguration
	 * cai no fallback lazyBean(AuthenticationManager.class), que resolve o
	 * proprio bean authenticationManager -> recursao infinita (StackOverflowError).
	 */
	@Bean
	UserDetailsService userDetailsService(UserRepository repo) {
		return email -> repo.findByEmail(email.trim().toLowerCase())
				.map(u -> org.springframework.security.core.userdetails.User
						.withUsername(u.getEmail())
						.password(u.getPassword())
						.authorities("ROLE_" + u.getGlobalRole().name())
						.disabled(!u.getActiveUser())
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado: " + email));
	}
}
