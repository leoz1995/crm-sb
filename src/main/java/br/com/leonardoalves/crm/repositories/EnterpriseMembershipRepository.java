package br.com.leonardoalves.crm.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.leonardoalves.crm.domain.enterprise.EnterpriseMembership;
import br.com.leonardoalves.crm.domain.enums.GlobalRole;

public interface EnterpriseMembershipRepository extends JpaRepository<EnterpriseMembership, UUID>{

	@Query(""" 
			SELECT COUNT(m) > 0
			FROM EnterpriseMembership m
			WHERE m.enterprise.id = :enterpriseId
			AND LOWER(m.user.email) = LOWER(:email)
			AND m.role = :role
			""")
	boolean existsByEnterpriseAndUserAndRole(@Param("enterpriseId") UUID enterpriseId, 
			@Param("email") String email, @Param("role") GlobalRole role);
	
	@Modifying
	@Query("""
			DELETE FROM EnterpriseMembership m
			WHERE m.enterprise.id = :enterpriseId
			""")
	int deleteAllByEnterpriseId(@Param("enterpriseId") UUID enterpriseId);
}
