package br.com.leonardoalves.crm.services.exception;

import org.springframework.http.HttpStatus;

public class RecursoNaoEncontradoException extends CrmException {

	private static final long serialVersionUID = 1L;

	public RecursoNaoEncontradoException(String recurso, Object id) {
		super(HttpStatus.NOT_FOUND, "recurso-nao-encontrado",
				"%s não encontrado: %s".formatted(recurso, id));
	}
}
