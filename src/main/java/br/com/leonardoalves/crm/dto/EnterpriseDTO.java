package br.com.leonardoalves.crm.dto;

import java.io.Serializable;
import java.time.Instant;

import br.com.leonardoalves.crm.domain.enterprise.Enterprise;

public class EnterpriseDTO implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String name;
	private String cnpj;
	private String email;
	private String phone; // telefone
	private String zipCode; // código postal
	private String street; // rua
	private String complement; // complemento
	private String neighborhood; // bairro
	private String city; // cidade
	private String state; // estado
	private Instant createdAt; // Data do cadastro
   	private boolean active = true; // empresa ativa
	
	public EnterpriseDTO() {}

	public EnterpriseDTO(Enterprise enterprise) {
		super();
		this.name = enterprise.getName();
		this.cnpj = enterprise.getCnpj();
		this.email = enterprise.getEmail();
		this.phone = enterprise.getPhone();
		this.zipCode = enterprise.getZipCode();
		this.street = enterprise.getStreet();
		this.complement = enterprise.getComplement();
		this.neighborhood = enterprise.getNeighborhood();
		this.city = enterprise.getCity();
		this.state = enterprise.getState();
		this.createdAt = enterprise.getCreatedAt();
		this.active = enterprise.isActive();
	}

	public String getName() {
		return name;
	}

	public String getCnpj() {
		return cnpj;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public String getZipCode() {
		return zipCode;
	}

	public String getStreet() {
		return street;
	}

	public String getComplement() {
		return complement;
	}
	
	public String getNeighborhood() {
		return neighborhood;
	}

	public String getCity() {
		return city;
	}
	
	public String getState() {
		return state;
	}
	
	public Instant getCreatedAt() {
		return createdAt;
	}

	public boolean isActive() {
		return active;
	}
}
