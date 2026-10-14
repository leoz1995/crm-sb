package br.com.leonardoalves.crm.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.leonardoalves.crm.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>{

	boolean existsByEmail(String email);
	
	Optional<User> findByEmail(String email);
}
