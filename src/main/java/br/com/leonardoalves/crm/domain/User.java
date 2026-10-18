package br.com.leonardoalves.crm.domain;

import java.util.UUID;

import org.hibernate.Hibernate;
import org.hibernate.annotations.UuidGenerator;

import br.com.leonardoalves.crm.domain.enums.EnterpriseRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name="all_users")
public class User{
	
	@Id
	@GeneratedValue
	@UuidGenerator(style=UuidGenerator.Style.TIME) // Solução moderna, ordenado por tempo
	private UUID id; //User identification
	private String name; //User name
	
	@NotBlank // Validar antes de chegar ao banco
	@Column(nullable=false, unique=true, length=254) // Não pode ser nullo, é unico e máximo de 254 caracteres
	private String email; //User mail (unique)
	private String password; //user password
	
	@Enumerated(EnumType.STRING)
	@Column(nullable=false, length=30)
	private EnterpriseRole globalRole;
	
	private boolean activeUser = true;
	
	@Version
	private Long version;

	protected User() {}

	public User(String name, String email, String password) {
		super();
		this.name = name;
		this.email = email;
		this.password = password;
	}

	public void setId(UUID id) {
		this.id = id;
	}
	
	public UUID getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = (email == null) ? null : email.trim().toLowerCase(); // Não grava desnormalizado
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		if(password == null || password.isBlank()) {
			throw new IllegalArgumentException("Senha obrigatória");
		}
		this.password = password;
	}

	public EnterpriseRole getGlobalRole() {
		return globalRole;
	}
	
	public boolean isActiveUser() {
		return activeUser;
	}
	
	public void setActiveUser(boolean activeUser) {
		this.activeUser = activeUser;
	}
	
	@Override
	public int hashCode() {
		return Hibernate.getClass(this).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || Hibernate.getClass(this) != Hibernate.getClass(obj))
			return false;
		User other = (User) obj;
		return id != null && id.equals(other.id);
	}
	
}
