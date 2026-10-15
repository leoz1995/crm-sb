package br.com.leonardoalves.crm.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.leonardoalves.crm.domain.employees.EnterpriseEmployee;

public interface EnterpriseEmployeeRepository extends JpaRepository<EnterpriseEmployee, UUID>{
	
	@Modifying
	@Query("""
			DELETE FROM EnterpriseEmployee ee
			WHERE ee.enterprise.id = :enterpriseId
			""")
	int deleteAllByEnterpriseId(@Param("enterpriseId") UUID enterpriseId);

}
