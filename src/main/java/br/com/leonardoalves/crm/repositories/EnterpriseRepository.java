package br.com.leonardoalves.crm.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.leonardoalves.crm.domain.enterprise.Enterprise;

public interface EnterpriseRepository  extends JpaRepository<Enterprise, UUID>{

	boolean existsByCnpj(String cnpj);
	
	@Query("""
		    SELECT DISTINCT e
		    FROM Enterprise e
		    JOIN e.membership m
		    WHERE LOWER(m.user.email) = LOWER(:email)
		    """)
		List<Enterprise> findAllAccessibleByUserEmail(
		        @Param("email") String email);
	
    @Query("""
            SELECT DISTINCT e
            FROM Enterprise e
            JOIN e.membership m
            WHERE e.id = :id
              AND LOWER(m.user.email) = LOWER(:email)
            """)
        Optional<Enterprise> findAccessibleByIdAndUserEmail(
                @Param("id") UUID id,
                @Param("email") String email);
    
	boolean existsByEmail(String email);
}
