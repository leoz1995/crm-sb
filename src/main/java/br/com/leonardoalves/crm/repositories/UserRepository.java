package br.com.leonardoalves.crm.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.leonardoalves.crm.domain.User;

public interface UserRepository extends JpaRepository<User, UUID>{

	boolean existsByEmail(String email);
	
	Optional<User> findByEmail(String email);
}
