package br.com.leonardoalves.crm.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.leonardoalves.crm.domain.User;
import br.com.leonardoalves.crm.repository.UserRepository;
import br.com.leonardoalves.crm.services.exception.ObjectNotFoundException;

@Service
public class UserService {

	private final UserRepository repo;
	
	UserService(UserRepository uRepository){
		this.repo = uRepository;
	}
	
	public User create(User u) {	
		if(repo.existsByEmail(u.getEmail())) {
			throw new IllegalArgumentException(
					"Este e-mail já está em uso.");
		}
		return repo.save(u);
	}
	
	public List<User> findAll(){
		return repo.findAll(); //Retornando todos os usuários.
	}
	
	public User findById(UUID uuid) {
		User user = repo.findById(uuid)
				.orElseThrow(() -> new ObjectNotFoundException("Objeto não encontrado."));
		
		return user;
	}
}
