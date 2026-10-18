package br.com.leonardoalves.crm.domain.enterprise;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Hibernate;

import br.com.leonardoalves.crm.domain.employees.EnterpriseEmployee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name="enterprises")
public class Enterprise{

	@Id
	@GeneratedValue(strategy=GenerationType.UUID)
	private UUID id;
	
	@Column(nullable=false, length=150)
	private String name;
	
	@Column(nullable=false, unique=true, length=14)
	private String cnpj;

	@Column(unique=true, length=254)
	private String email;
	
	@Column(length = 20)
	private String phone; // telefone

	@Column(length=8)
	private String zipCode; // código postal

	@Column(length=150)
	private String street; // rua

	@Column(length=100)
	private String complement; // complemento

	@Column(length=100)
	private String neighborhood; // bairro

	@Column(length=100)
	private String city; // cidade

	@Column(length=2)
	private String state; // estado
	
    @Column(nullable = false, updatable = false)
	private Instant createdAt; // Data do cadastro
	private Instant updatedAt; // Último update
	
	@Column(name="deleted_at")
	private Instant deletedAt;
    
	@Column(nullable = false)
	private boolean active = true; // empresa ativa
	
	@OneToMany(mappedBy="enterprise", fetch=FetchType.LAZY)
	private Set<EnterpriseEmployee> employeeLinks = new HashSet<>();
	
	@OneToMany(mappedBy="enterprise", fetch=FetchType.LAZY)
	private Set<EnterpriseMembership> membership = new HashSet<>();
	
	@Version
	private Long version;
	
	protected Enterprise() {}
	public Enterprise(String name, String cnpj, String email, String phone, String zipCode, String street,
			String complement, String neighborhood, String city, String state) {
		super();
		this.name = name;
		this.cnpj = cnpj == null ? null : cnpj.replaceAll("\\D", "");
		this.email = email;
		this.phone = phone;
		this.zipCode = zipCode;
		this.street = street;
		this.complement = complement;
		this.neighborhood = neighborhood;
		this.city = city;
		this.state = state;
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
	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}
	public String getCnpj() {
		return cnpj;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public String getComplement() {
		return complement;
	}
	public void setComplement(String complement) {
		this.complement = complement;
	}
	public String getNeighborhood() {
		return neighborhood;
	}
	public void setNeighborhood(String neighborhood) {
		this.neighborhood = neighborhood;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public Instant getCreatedAt() {
		return createdAt;
	}
	public Instant getUpdatedAt() {
		return updatedAt;
	}
	public Instant getDeletedAt() {
		return deletedAt;
	}
	public boolean isDeleted() {
		return deletedAt != null;
	}
	public void markAsDeleted() {
		this.deletedAt = Instant.now();
	}
	public void restore() {
		this.deletedAt = null;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public Set<EnterpriseEmployee> getEmployeeLinks() {
		return employeeLinks;
	}
	public Set<EnterpriseMembership> getMembership() {
		return Collections.unmodifiableSet(membership); //expor só leitura.
	}
	@PrePersist
	public void prePersist() {
		createdAt = Instant.now();
		updatedAt = createdAt;
	}
	
	@PreUpdate
	public void preUpdate() {
		updatedAt = Instant.now();
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

		Enterprise other = (Enterprise) obj;
		return id != null && id.equals(other.id);
	}
}
