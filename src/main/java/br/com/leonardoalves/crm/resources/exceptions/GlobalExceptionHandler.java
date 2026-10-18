package br.com.leonardoalves.crm.resources.exceptions;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import br.com.leonardoalves.crm.services.exception.CrmException;
import br.com.leonardoalves.crm.services.exception.ObjectNotFoundException;
import br.com.leonardoalves.crm.services.exception.PasswordNotMatchException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

/**
 * Cobre apenas o que e lancado DENTRO dos controllers. Token ausente ou invalido
 * e barrado antes, pelo BearerTokenAuthenticationFilter, e continua saindo como
 * 401 com o header WWW-Authenticate.
 *
 * E Error nao e Exception: StackOverflowError e OutOfMemoryError nao chegam aqui
 * — vao para o dispatch de /error, que por isso precisa de permitAll().
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private static final String BASE_TIPO = "https://api.crm.com.br/erros/";

	// ===================== negocio =====================

	@ExceptionHandler(CrmException.class)
	ProblemDetail crm(CrmException e, HttpServletRequest req) {
		log.debug("Erro de negocio [{}] em {}: {}", e.getTipo(), req.getRequestURI(), e.getMessage());
		return problema(e.getStatus(), e.getTipo(), e.getMessage(), req);
	}

	@ExceptionHandler(ObjectNotFoundException.class)
	ProblemDetail naoEncontrado(ObjectNotFoundException e, HttpServletRequest req) {
		return problema(HttpStatus.NOT_FOUND, "recurso-nao-encontrado", e.getMessage(), req);
	}

	@ExceptionHandler(PasswordNotMatchException.class)
	ProblemDetail senhaInvalida(PasswordNotMatchException e, HttpServletRequest req) {
		return problema(HttpStatus.BAD_REQUEST, "senha-invalida", e.getMessage(), req);
	}

	// ===================== autenticacao / autorizacao =====================

	/**
	 * Conta existe mas esta impedida de entrar. Status proprio porque reenviar a
	 * senha nao resolve: quem ve isso precisa do suporte.
	 *
	 * TRADE-OFF: este 403 confirma que a conta EXISTE. Se o modelo de ameaca nao
	 * aceitar isso, junte com o handler de AuthenticationException abaixo.
	 */
	@ExceptionHandler({ DisabledException.class, LockedException.class })
	ProblemDetail contaIndisponivel(AuthenticationException e, HttpServletRequest req) {
		log.info("Login barrado por estado da conta em {}: {}",
				req.getRequestURI(), e.getClass().getSimpleName());

		return problema(HttpStatus.FORBIDDEN, "conta-indisponivel",
				"Esta conta está desativada ou bloqueada. Procure o suporte.", req);
	}

	/**
	 * BadCredentialsException e UsernameNotFoundException caem aqui com a MESMA
	 * resposta, de proposito: distinguir "senha errada" de "e-mail inexistente"
	 * entrega a um atacante a lista de e-mails cadastrados.
	 */
	@ExceptionHandler(AuthenticationException.class)
	ProblemDetail autenticacaoFalhou(AuthenticationException e, HttpServletRequest req) {
		log.info("Falha de autenticacao em {}: {}", req.getRequestURI(), e.getClass().getSimpleName());

		return problema(HttpStatus.UNAUTHORIZED, "credenciais-invalidas",
				"E-mail ou senha inválidos.", req);
	}

	/** Lancada pelo @PreAuthorize. */
	@ExceptionHandler(AccessDeniedException.class)
	ProblemDetail acessoNegado(AccessDeniedException e, HttpServletRequest req) {
		log.info("Acesso negado em {} {}", req.getMethod(), req.getRequestURI());

		return problema(HttpStatus.FORBIDDEN, "acesso-negado",
				"Você não tem permissão para esta operação.", req);
	}

	// ===================== integridade / concorrencia =====================

	/**
	 * A constraint do banco e a garantia real contra corrida (o TOCTOU do
	 * existsBy). Aqui a violacao e traduzida pelo NOME da constraint.
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	ProblemDetail integridade(DataIntegrityViolationException e, HttpServletRequest req) {
		String causa = e.getMostSpecificCause().getMessage();
		log.warn("Violacao de integridade em {}: {}", req.getRequestURI(), causa);

		if (contem(causa, "email")) {
			return problema(HttpStatus.CONFLICT, "email-em-uso",
					"Este e-mail já está em uso.", req);
		}
		if (contem(causa, "uk_enterprise_membership_user_enterprise")) {
			return problema(HttpStatus.CONFLICT, "membro-duplicado",
					"Este usuário já pertence a esta empresa.", req);
		}
		if (contem(causa, "cnpj")) {
			return problema(HttpStatus.CONFLICT, "cnpj-em-uso",
					"Já existe uma empresa cadastrada com este CNPJ.", req);
		}
		if (contem(causa, "foreign key")) {
			return problema(HttpStatus.CONFLICT, "registro-em-uso",
					"Este registro está vinculado a outros e não pode ser removido.", req);
		}
		// NUNCA devolver 'causa': ela traz nome de tabela, de coluna e o valor.
		return problema(HttpStatus.CONFLICT, "conflito-de-dados",
				"A operação conflita com dados existentes.", req);
	}

	@ExceptionHandler(OptimisticLockingFailureException.class)
	ProblemDetail concorrencia(OptimisticLockingFailureException e, HttpServletRequest req) {
		return problema(HttpStatus.CONFLICT, "registro-alterado",
				"Este registro foi alterado por outra pessoa. Recarregue e tente novamente.", req);
	}

	// ===================== validacao =====================

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex, HttpHeaders headers,
			HttpStatusCode status, WebRequest request) {

		Map<String, String> campos = new LinkedHashMap<>();
		for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
			campos.putIfAbsent(fe.getField(),
					fe.getDefaultMessage() == null ? "inválido" : fe.getDefaultMessage());
		}

		ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		pd.setType(URI.create(BASE_TIPO + "dados-invalidos"));
		pd.setTitle("Dados inválidos");
		pd.setDetail("Verifique os campos informados.");
		pd.setProperty("campos", campos);
		pd.setProperty("timestamp", Instant.now().toString());

		return ResponseEntity.badRequest().body(pd);
	}

	/** Lancada por @Validated em parametro de metodo. */
	@ExceptionHandler(ConstraintViolationException.class)
	ProblemDetail violacao(ConstraintViolationException e, HttpServletRequest req) {
		Map<String, String> campos = new LinkedHashMap<>();
		e.getConstraintViolations().forEach(v ->
				campos.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));

		ProblemDetail pd = problema(HttpStatus.BAD_REQUEST, "dados-invalidos",
				"Verifique os campos informados.", req);
		pd.setProperty("campos", campos);
		return pd;
	}

	/** GET /users/abc -> UUID invalido. Antes disto era 500. */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ProblemDetail tipoInvalido(MethodArgumentTypeMismatchException e, HttpServletRequest req) {
		return problema(HttpStatus.BAD_REQUEST, "parametro-invalido",
				"O valor informado em '%s' não é válido.".formatted(e.getName()), req);
	}

	// ===================== rede de seguranca =====================

	/**
	 * ULTIMO RECURSO. Se cair aqui e BUG — por isso log.error com stack completo,
	 * e por isso a resposta NAO contem e.getMessage().
	 */
	@ExceptionHandler(Exception.class)
	ProblemDetail inesperado(Exception e, HttpServletRequest req) {
		String traceId = UUID.randomUUID().toString().substring(0, 8);

		log.error("Erro inesperado [traceId={}] em {} {}",
				traceId, req.getMethod(), req.getRequestURI(), e);

		ProblemDetail pd = problema(HttpStatus.INTERNAL_SERVER_ERROR, "erro-interno",
				"Ocorreu um erro inesperado. Informe o código ao suporte.", req);
		pd.setProperty("traceId", traceId);
		return pd;
	}

	// ===================== helpers =====================

	private ProblemDetail problema(HttpStatus status, String tipo, String detalhe,
			HttpServletRequest req) {
		ProblemDetail pd = ProblemDetail.forStatus(status);
		pd.setType(URI.create(BASE_TIPO + tipo));
		pd.setTitle(titulo(status));
		pd.setDetail(detalhe);
		pd.setInstance(URI.create(req.getRequestURI()));
		pd.setProperty("timestamp", Instant.now().toString());
		return pd;
	}

	private static String titulo(HttpStatus s) {
		return switch (s) {
			case BAD_REQUEST -> "Requisição inválida";
			case UNAUTHORIZED -> "Não autenticado";
			case FORBIDDEN -> "Acesso negado";
			case NOT_FOUND -> "Não encontrado";
			case CONFLICT -> "Conflito";
			case UNPROCESSABLE_CONTENT -> "Não foi possível processar";
			case TOO_MANY_REQUESTS -> "Muitas requisições";
			default -> "Erro";
		};
	}

	private static boolean contem(String texto, String agulha) {
		return texto != null && texto.toLowerCase().contains(agulha.toLowerCase());
	}
}
