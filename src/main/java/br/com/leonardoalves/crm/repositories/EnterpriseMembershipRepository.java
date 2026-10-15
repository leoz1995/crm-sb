package br.com.leonardoalves.crm.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.leonardoalves.crm.domain.enterprise.EnterpriseMembership;

public interface EnterpriseMembershipRepository extends JpaRepository<EnterpriseMembership, UUID>{

}
