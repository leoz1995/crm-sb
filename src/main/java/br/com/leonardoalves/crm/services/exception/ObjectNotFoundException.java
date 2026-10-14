package br.com.leonardoalves.crm.services.exception;

public class ObjectNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public ObjectNotFoundException(String m) {
		super(m);
	}
}
