package br.com.leonardoalves.crm.dto;

import java.io.Serializable;

import br.com.leonardoalves.crm.domain.User;

public class UserDTO implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String name; //User name
	private String email; //User mail (unique)
	
	public UserDTO() {}
	
	public UserDTO(User user) {
		name = user.getName();
		email = user.getName();
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
	
}
