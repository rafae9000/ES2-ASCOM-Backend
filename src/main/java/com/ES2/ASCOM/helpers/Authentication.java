package com.ES2.ASCOM.helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.ES2.ASCOM.exception.ApiRequestException;
import com.ES2.ASCOM.model.Usuario;
import com.ES2.ASCOM.repository.UsuarioDAO;

@Service
public class Authentication {
	
	@Autowired
	private UsuarioDAO usuarioDAO;
	private TokenService tokenService = new TokenService();
	
	public Usuario authenticateUser(String token) throws ApiRequestException {
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);
		
		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		return logged_user;
	}
}
