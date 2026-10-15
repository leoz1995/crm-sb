package br.com.leonardoalves.crm.dto;

import java.time.Instant;

public class EnterpriseCreateDTO {
	
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
	private Instant updatedAt; // Último update
    
	private boolean active = true; // empresa ativa
	
	public EnterpriseCreateDTO() {}
	public EnterpriseCreateDTO(String name, String cnpj, String email, String phone, String zipCode, String street,
			String complement, String neighborhood, String city, String state) {
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
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
}
