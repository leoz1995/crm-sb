package br.com.leonardoalves.crm.services;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.leonardoalves.crm.domain.User;
import br.com.leonardoalves.crm.dto.UserCreateDTO;
import br.com.leonardoalves.crm.repository.UserRepository;
import br.com.leonardoalves.crm.services.exception.ObjectNotFoundException;
import br.com.leonardoalves.crm.services.exception.PasswordNotMatchException;

@Service
public class UserService {

	private final UserRepository repo;
	private final PasswordEncoder passwordEncoder;
	
	UserService(UserRepository uRepository, PasswordEncoder passwordEncoder){
		this.repo = uRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	public User create(User u) {	
		
		String email = u.getEmail().trim().toLowerCase();
		
		if(repo.existsByEmail(email)) {
			throw new IllegalArgumentException(
					"Este e-mail já está em uso.");
		}
		u.setEmail(email);
		
		// Codificando a senha antes de salvar
		u.setPassword(passwordEncoder.encode(u.getPassword()));
		
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
	
	public void delete (UUID id) {
		findById(id);
		repo.deleteById(id);
	}
	
	public User update(User user) {
		User existsUser = repo.findById(user.getId())
				.orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado."));
		
		updateData(existsUser, user);
		
		return repo.save(existsUser);
	}
	
	private void updateData(User existsUser, User user) {
		boolean passMatch = passwordEncoder.matches(user.getPassword(), existsUser.getPassword());
		
	    if (!passMatch) {
	        throw new PasswordNotMatchException();
	    }
	    
		existsUser.setName(user.getName());
		existsUser.setEmail(user.getEmail());
		existsUser.setPassword(passwordEncoder.encode(user.getPassword()));
	}

	public User fromDTO(UserCreateDTO userCreateDTO) {
		return new User(userCreateDTO.getName(), userCreateDTO.getEmail(), userCreateDTO.getPassword());
	}
}
