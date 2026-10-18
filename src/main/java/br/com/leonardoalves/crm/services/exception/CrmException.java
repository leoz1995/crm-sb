package br.com.leonardoalves.crm.services.exception;

import org.springframework.http.HttpStatus;

/**
 * Raiz de todo erro de negocio do CRM.
 *
 * O construtor desliga o stack trace: excecao de controle de fluxo nao e bug, e
 * fillInStackTrace() e a parte caríssima de criar excecao. Sob forca bruta no
 * login, isso e consumo de CPU real.
 */
public abstract class CrmException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final HttpStatus status;
	private final String tipo;

	protected CrmException(HttpStatus status, String tipo, String mensagem) {
		super(mensagem, null, false, false);
		this.status = status;
		this.tipo = tipo;
	}

	public HttpStatus getStatus() {
		return status;
	}

	/** Vira o campo "type" do ProblemDetail: o cliente trata por ele, nao pela mensagem. */
	public String getTipo() {
		return tipo;
	}
}
