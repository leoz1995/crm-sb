package br.com.leonardoalves.crm.resources;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.leonardoalves.crm.dto.user.TrocaSenhaRequest;
import br.com.leonardoalves.crm.dto.user.UserCreateRequest;
import br.com.leonardoalves.crm.dto.user.UserPatchRequest;
import br.com.leonardoalves.crm.dto.user.UserResponse;
import br.com.leonardoalves.crm.services.UserService;
import br.com.leonardoalves.crm.services.exception.RegraDeNegocioException;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/v1/users")
public class UserResource {

	private final UserService service;

	UserResource(UserService service) {
		this.service = service;
	}

	@GetMapping
	@PreAuthorize("hasRole('PLATFORM_ADMIN')")
	public Page<UserResponse> listar(
			@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
			Pageable pageable) {
		return service.list(pageable);
	}

	/** Rota propria para o proprio perfil: o cliente nao precisa saber o proprio UUID. */
	@GetMapping("/me")
	public UserResponse eu(@AuthenticationPrincipal Jwt jwt) {
		return service.search(idDo(jwt));
	}

	@PatchMapping("/me")
	public UserResponse alterarMeuPerfil(@Valid @RequestBody UserPatchRequest req,
			@AuthenticationPrincipal Jwt jwt) {
		return service.change(idDo(jwt), req);
	}

	/**
	 * Troca de senha em rota propria, porque exige a senha atual: sem isso um
	 * token roubado tomaria a conta permanentemente.
	 */
	@PutMapping("/me/senha")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void trocarMinhaSenha(@Valid @RequestBody TrocaSenhaRequest req,
			@AuthenticationPrincipal Jwt jwt) {
		service.changePassword(idDo(jwt), req);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('PLATFORM_ADMIN') "
			+ "or #id.toString() == authentication.principal.claims['uid']")
	public UserResponse buscar(@PathVariable UUID id) {
		return service.search(id);
	}

	@PostMapping
	@PreAuthorize("hasRole('PLATFORM_ADMIN')")
	public ResponseEntity<UserResponse> criar(@Valid @RequestBody UserCreateRequest req) {
		UserResponse criado = service.create(req);

		URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(criado.id()).toUri();

		// 201 + Location + o corpo: sem o corpo o cliente precisaria de um GET
		// extra so para descobrir o id.
		return ResponseEntity.created(uri).body(criado);
	}

	@PatchMapping("/{id}")
	@PreAuthorize("hasRole('PLATFORM_ADMIN')")
	public UserResponse alterar(@PathVariable UUID id,
			@Valid @RequestBody UserPatchRequest req) {
		return service.change(id, req);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('PLATFORM_ADMIN')")
	public void desativar(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
		// Sem esta guarda, o unico admin da plataforma pode se desativar e
		// ninguem mais consegue administrar — sem acesso ao banco, nao ha volta.
		if (id.toString().equals(jwt.getClaimAsString("uid"))) {
			throw new RegraDeNegocioException("auto-desativacao",
					"Você não pode desativar a própria conta.");
		}
		service.desactivate(id);
	}

	private static UUID idDo(Jwt jwt) {
		return UUID.fromString(jwt.getClaimAsString("uid"));
	}
}
