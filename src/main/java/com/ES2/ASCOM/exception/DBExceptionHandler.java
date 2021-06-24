package com.ES2.ASCOM.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import org.springframework.http.HttpStatus;

@ControllerAdvice
public class DBExceptionHandler {
	
	@ExceptionHandler(value = {SQLException.class})
	public ResponseEntity<Object> handler(SQLException e){
		ApiException apiException = new ApiException("Houver uma falha interna na conexão com o banco", HttpStatus.INTERNAL_SERVER_ERROR);
		e.printStackTrace();
		//int number = e.getStackTrace()[0].getLineNumber();
		//String classe = e.getStackTrace()[0].getClassName();
	    //System.out.println(classe+" : " + number);
		return new ResponseEntity<>(apiException,apiException.getStatus());
	}

}
