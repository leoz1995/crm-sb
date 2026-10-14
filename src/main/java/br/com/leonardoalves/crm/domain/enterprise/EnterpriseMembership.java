package br.com.leonardoalves.crm.domain.enterprise;

import java.util.UUID;

import br.com.leonardoalves.crm.domain.User;
import br.com.leonardoalves.crm.domain.enums.EnterpriseRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name="enterprise_membership", uniqueConstraints=@UniqueConstraint(columnNames= {"user_id", "enterprise_id"}))
public class EnterpriseMembership {
	
	@Id
	@GeneratedValue(strategy=GenerationType.UUID)
	private UUID ui;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="user_id", nullable=false)
	private User user;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="enterprise_id", nullable=false)
	private Enterprise enterprise;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable=false, length=30)
	private EnterpriseRole role = EnterpriseRole.MEMBER;
	
	protected EnterpriseMembership() {}
	
	public EnterpriseMembership(User user, Enterprise enterprise, EnterpriseRole role) {
		this.user = user;
		this.enterprise = enterprise;
		this.role = role;
	}
}
