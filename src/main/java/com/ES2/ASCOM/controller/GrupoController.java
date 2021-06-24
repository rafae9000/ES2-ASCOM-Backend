package com.ES2.ASCOM.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ES2.ASCOM.exception.ApiRequestException;
import com.ES2.ASCOM.helpers.TokenService;
import com.ES2.ASCOM.model.Grupo;
import com.ES2.ASCOM.model.Usuario;
import com.ES2.ASCOM.repository.GrupoDAO;
import com.ES2.ASCOM.repository.UsuarioDAO;
@RestController
@RequestMapping("/grupo")
public class GrupoController {

	@Autowired
	private GrupoDAO grupoDAO;
	@Autowired
	private UsuarioDAO usuarioDAO;
	private TokenService tokenService = new TokenService();
	
	@GetMapping("/listar")
	public List<Grupo> listar(@RequestHeader Map<String,String> header) throws ApiRequestException{
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);
		
		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		return grupoDAO.findAll();
	}
}
