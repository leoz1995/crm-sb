package br.com.leonardoalves.crm.domain;

import java.io.Serializable;
import java.util.UUID;

import org.hibernate.Hibernate;

import br.com.leonardoalves.crm.domain.enums.GlobalRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="all_users")
public class User implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.UUID) //Auto implementação
	private UUID id; //User identification
	private String name; //User name
	@Column(nullable=false, unique=true, length=254) // Não pode ser nullo, é unico e máximo de 254 caracteres
	private String email; //User mail (unique)
	private String password; //user password
	
	@Enumerated(EnumType.STRING)
	@Column(nullable=false, length=30)
	private GlobalRole globalRole = GlobalRole.USER;
	
	private boolean activeUser = true;

	public User() {}

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
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public GlobalRole getGlobalRole() {
		return globalRole;
	}
	
	public boolean getActiveUser() {
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
