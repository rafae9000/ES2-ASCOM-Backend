package com.ES2.ASCOM.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler {
	
	@ExceptionHandler(value = {ApiRequestException.class})
	public ResponseEntity<Object> handler(ApiRequestException e){
		ApiException apiException = new ApiException(e.getMessage(), e.getStatus());
		e.printStackTrace();
		//int number = e.getStackTrace()[0].getLineNumber();
		//String classe = e.getStackTrace()[0].getClassName();
	    //System.out.println(classe+" : " + number);
		return new ResponseEntity<>(apiException,apiException.getStatus());
	}
}
