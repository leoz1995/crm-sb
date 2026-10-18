package br.com.leonardoalves.crm.services.exception;

import org.springframework.http.HttpStatus;

public class EmailEmUsoException extends CrmException {

	private static final long serialVersionUID = 1L;

	public EmailEmUsoException() {
		super(HttpStatus.CONFLICT, "email-em-uso", "Este e-mail já está em uso.");
	}
}
