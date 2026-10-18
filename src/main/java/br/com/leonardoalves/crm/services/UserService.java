package br.com.leonardoalves.crm.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.leonardoalves.crm.domain.User;
import br.com.leonardoalves.crm.dto.user.TrocaSenhaRequest;
import br.com.leonardoalves.crm.dto.user.UserCreateRequest;
import br.com.leonardoalves.crm.dto.user.UserPatchRequest;
import br.com.leonardoalves.crm.dto.user.UserResponse;
import br.com.leonardoalves.crm.repositories.UserRepository;
import br.com.leonardoalves.crm.services.exception.EmailEmUsoException;
import br.com.leonardoalves.crm.services.exception.RecursoNaoEncontradoException;
import br.com.leonardoalves.crm.services.exception.RegraDeNegocioException;

@Service
public class UserService {

	private final UserRepository repo;
	private final PasswordEncoder passwordEncoder;

	UserService(UserRepository uRepository, PasswordEncoder passwordEncoder) {
		this.repo = uRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public UserResponse create(UserCreateRequest req) {
		// A checagem explicita da a MENSAGEM boa; a garantia contra corrida
		// (duas requisicoes simultaneas) e a constraint unique do banco, tratada
		// no GlobalExceptionHandler.
		if (repo.existsByEmail(req.email())) {
			throw new EmailEmUsoException();
		}
		User u = new User(req.name(), req.email(), passwordEncoder.encode(req.password()));

		return UserResponse.de(repo.save(u));
	}

	@Transactional(readOnly = true)
	public Page<UserResponse> list(Pageable pageable) {
		return repo.findAll(pageable).map(UserResponse::de);
	}

	@Transactional(readOnly = true)
	public UserResponse search(UUID id) {
		return UserResponse.de(searchEntity(id));
	}

	@Transactional
	public UserResponse change(UUID id, UserPatchRequest req) {
		User user = searchEntity(id);

		if (req.name() != null) {
			user.setName(req.name());
		}
		if (req.email() != null && !req.email().equals(user.getEmail())) {
			// create() checava duplicidade e update() nao: era violacao de FK
			// virando 500.
			if (repo.existsByEmail(req.email())) {
				throw new EmailEmUsoException();
			}
			user.setEmail(req.email());
		}

		return UserResponse.de(user);
	}

	/**
	 * Duas comparacoes DISTINTAS, e e aqui que estava a condicao invertida:
	 * a senha atual tem de BATER (prova de identidade) e a nova tem de NAO bater.
	 */
	@Transactional
	public void changePassword(UUID id, TrocaSenhaRequest req) {
		User user = searchEntity(id);

		if (!passwordEncoder.matches(req.senhaAtual(), user.getPassword())) {
			throw new BadCredentialsException("Senha atual incorreta");
		}
		if (passwordEncoder.matches(req.senhaNova(), user.getPassword())) {
			throw new RegraDeNegocioException("senha-repetida",
					"A nova senha não pode ser igual à atual.");
		}

		user.setPassword(passwordEncoder.encode(req.senhaNova()));
	}

	/**
	 * Soft delete: deleteById() estourava violacao de FK em usuario com
	 * EnterpriseMembership, e apagar destruiria o historico de quem criou o que.
	 */
	@Transactional
	public void desactivate(UUID id) {
		searchEntity(id).setActiveUser(false);
	}

	@Transactional(readOnly=true)
	private User searchEntity(UUID id) {
		return repo.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
	}
}
