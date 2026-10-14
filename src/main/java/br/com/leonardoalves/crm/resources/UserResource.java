package br.com.leonardoalves.crm.resources;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.leonardoalves.crm.domain.User;
import br.com.leonardoalves.crm.dto.UserDTO;
import br.com.leonardoalves.crm.services.UserService;

@RestController
@RequestMapping(value="/api/v1/users")
public class UserResource {
	
	private final UserService service;
	
	UserResource(UserService service){
		this.service = service;
	}
	
	@PostMapping
	public ResponseEntity<User> addUser(@RequestBody User u){
		u = service.create(u);
		
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(u.getId()).toUri();
		
		return ResponseEntity.created(uri).body(u);
	}
	
	//@RequestMapping(method=RequestMethod.GET)
	@GetMapping
	public ResponseEntity<List<UserDTO>> findAll(){
		List<User> list = service.findAll();
		List<UserDTO> listDTO = list.stream().map(u -> new UserDTO(u)).toList();
		return ResponseEntity.ok().body(listDTO);
	}
	
	
}
