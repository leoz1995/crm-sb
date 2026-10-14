package br.com.leonardoalves.crm.resources.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import br.com.leonardoalves.crm.services.exception.ObjectNotFoundException;
import br.com.leonardoalves.crm.services.exception.PasswordNotMatchException;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ResourceExceptionHandler {
	
	@ExceptionHandler(ObjectNotFoundException.class)
	public ResponseEntity<StandardError> notFound(ObjectNotFoundException e, HttpServletRequest req){
		
		HttpStatus sts = HttpStatus.NOT_FOUND;
		StandardError err = new StandardError(System.currentTimeMillis(), sts.value(), "Não encontrado", e.getMessage(), req.getRequestURI());
		
		return ResponseEntity.status(sts).body(err);
	}
	
	@ExceptionHandler(PasswordNotMatchException.class)
	public ResponseEntity<StandardError> passwordNotMatch(PasswordNotMatchException e,
	        HttpServletRequest req) {

	    HttpStatus status = HttpStatus.BAD_REQUEST;

	    StandardError err = new StandardError(
	        System.currentTimeMillis(),
	        status.value(),
	        "Senha inválida",
	        e.getMessage(),
	        req.getRequestURI()
	    );

	    return ResponseEntity.status(status).body(err);
	}
}
