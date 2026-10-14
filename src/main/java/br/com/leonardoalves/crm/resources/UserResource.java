package br.com.leonardoalves.crm.resources;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.leonardoalves.crm.domain.User;
import br.com.leonardoalves.crm.dto.UserCreateDTO;
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
	public ResponseEntity<Void> create(@RequestBody UserCreateDTO userDTO){	
		User user = service.fromDTO(userDTO);
		user = service.create(user);
		
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(user.getId()).toUri();
		
		return ResponseEntity.created(uri).build();
	}

	@RequestMapping(value="/{id}", method=RequestMethod.PUT)
	public ResponseEntity<Void> update(@RequestBody UserCreateDTO userDTO, @PathVariable UUID id){	
		User user = service.fromDTO(userDTO);
		user.setId(id);
		user = service.update(user);
		return ResponseEntity.noContent().build(); //Retorna 204
	}
	//@RequestMapping(method=RequestMethod.GET)
	@GetMapping
	public ResponseEntity<List<UserDTO>> findAll(){
		List<User> list = service.findAll();
		List<UserDTO> listDTO = list.stream().map(u -> new UserDTO(u)).toList();
		return ResponseEntity.ok().body(listDTO);
	}
	
	@RequestMapping(value="/{id}", method=RequestMethod.DELETE)
	public ResponseEntity<Void> delete(@PathVariable UUID id){	
		service.delete(id);
		return ResponseEntity.noContent().build(); //Retorna 204
	}
	
	@RequestMapping(value="/{id}", method=RequestMethod.GET)
	public ResponseEntity<UserDTO> findById(@PathVariable UUID id){	
		User user = service.findById(id);
		return ResponseEntity.ok().body(new UserDTO(user));
	}
}
