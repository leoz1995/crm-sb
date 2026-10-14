package br.com.leonardoalves.crm.services.exception;

public class PasswordNotMatchException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public PasswordNotMatchException(){
		super("A senha atual não confere.");
	}
}
