package br.com.leonardoalves.crm.resources.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import br.com.leonardoalves.crm.services.exception.ObjectNotFoundException;
import br.com.leonardoalves.crm.services.exception.PasswordNotMatchException;
import jakarta.servlet.http.HttpServletRequest;

/*
 * Cobre apenas o que e lancado DENTRO dos controllers. Token invalido ou
 * ausente e barrado antes, pelo BearerTokenAuthenticationFilter, e continua
 * saindo como 401 de corpo vazio com o header WWW-Authenticate.
 */
@ControllerAdvice
public class ResourceExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ResourceExceptionHandler.class);

	private static ResponseEntity<StandardError> build(HttpStatus status, String error, String message,
			HttpServletRequest req) {

		StandardError body = new StandardError(System.currentTimeMillis(), status.value(), error, message,
				req.getRequestURI());

		return ResponseEntity.status(status).body(body);
	}

	@ExceptionHandler(ObjectNotFoundException.class)
	public ResponseEntity<StandardError> notFound(ObjectNotFoundException e, HttpServletRequest req) {
		return build(HttpStatus.NOT_FOUND, "Não encontrado", e.getMessage(), req);
	}

	@ExceptionHandler(PasswordNotMatchException.class)
	public ResponseEntity<StandardError> passwordNotMatch(PasswordNotMatchException e, HttpServletRequest req) {
		return build(HttpStatus.BAD_REQUEST, "Senha inválida", e.getMessage(), req);
	}

	/*
	 * Conta existente mas impedida de entrar. Status proprio porque reenviar a
	 * senha nao resolve: quem ve isso precisa falar com o suporte.
	 */
	@ExceptionHandler({ DisabledException.class, LockedException.class })
	public ResponseEntity<StandardError> accountUnavailable(AuthenticationException e, HttpServletRequest req) {
		log.debug("Login barrado por estado da conta em {}: {}", req.getRequestURI(), e.getClass().getSimpleName());

		return build(HttpStatus.FORBIDDEN, "Conta indisponível",
				"Esta conta está desativada ou bloqueada. Procure o suporte.", req);
	}

	/*
	 * BadCredentialsException e UsernameNotFoundException caem aqui com a MESMA
	 * resposta, de proposito: distinguir "senha errada" de "e-mail inexistente"
	 * entrega a um atacante a lista de e-mails cadastrados. A causa real fica
	 * so no log.
	 */
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<StandardError> authenticationFailed(AuthenticationException e, HttpServletRequest req) {
		log.debug("Falha de autenticação em {}: {}", req.getRequestURI(), e.getClass().getSimpleName());

		return build(HttpStatus.UNAUTHORIZED, "Não autorizado", "E-mail ou senha inválidos.", req);
	}

	/*
	 * Regra de negocio violada: "Este e-mail ja esta em uso" (UserService) e
	 * "Usuario nao encontrado" (AuthService). Antes vazavam como 500.
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<StandardError> illegalArgument(IllegalArgumentException e, HttpServletRequest req) {
		return build(HttpStatus.BAD_REQUEST, "Requisição inválida", e.getMessage(), req);
	}
}
