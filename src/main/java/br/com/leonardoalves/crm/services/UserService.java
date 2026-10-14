package br.com.leonardoalves.crm.services;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.leonardoalves.crm.domain.User;
import br.com.leonardoalves.crm.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository uRepository;
	
	UserService(UserRepository uRepository){
		this.uRepository = uRepository;
	}
	
	public User create(User u) {	
		if(uRepository.existsByEmail(u.getEmail())) {
			throw new IllegalArgumentException(
					"Este e-mail já está em uso.");
		}
		return uRepository.save(u);
	}
	
	public List<User> findAll(){
		return uRepository.findAll(); //Retornando todos os usuários.
	}
}
