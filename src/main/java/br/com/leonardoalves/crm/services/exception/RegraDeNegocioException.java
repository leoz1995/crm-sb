package br.com.leonardoalves.crm.services.exception;

import org.springframework.http.HttpStatus;

public class RegraDeNegocioException extends CrmException {

	private static final long serialVersionUID = 1L;

	public RegraDeNegocioException(String tipo, String mensagem) {
		super(HttpStatus.UNPROCESSABLE_CONTENT, tipo, mensagem);
	}
}
