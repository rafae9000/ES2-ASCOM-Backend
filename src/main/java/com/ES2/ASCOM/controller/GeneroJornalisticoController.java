package com.ES2.ASCOM.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ES2.ASCOM.exception.ApiRequestException;
import com.ES2.ASCOM.helpers.TokenService;
import com.ES2.ASCOM.model.Chamado;
import com.ES2.ASCOM.model.GeneroJornalistico;
import com.ES2.ASCOM.model.Grupo;
import com.ES2.ASCOM.model.Usuario;
import com.ES2.ASCOM.pagination.Paginacao;
import com.ES2.ASCOM.repository.CustomUsuarioDAO;
import com.ES2.ASCOM.repository.GeneroJornalisticoDAO;
import com.ES2.ASCOM.repository.GrupoDAO;
import com.ES2.ASCOM.repository.PermissaoDAO;
import com.ES2.ASCOM.repository.UsuarioDAO;

@RestController
@RequestMapping("/generoJornalistico")
public class GeneroJornalisticoController {
	
	@Autowired
	private UsuarioDAO usuarioDAO;
	@Autowired
	private GrupoDAO grupoDAO; 
	@Autowired
	private GeneroJornalisticoDAO generoJornalisticoDAO;
	
	private TokenService tokenService = new TokenService();
	private final Paginacao<GeneroJornalistico> pag = new Paginacao<GeneroJornalistico>();
	
	private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private Validator validator = factory.getValidator();
	
	@GetMapping("/listar")
	public Map<String,Object> listar(@RequestHeader Map<String,String> header, 
									 @RequestParam(required = false) Integer paginaAtual, 
									 @RequestParam(required = false) Integer tamanhoPagina) throws ApiRequestException{
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);
		
		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		List<GeneroJornalistico> lista = generoJornalisticoDAO.findAll();
		
		return pag.paginarLista(paginaAtual, tamanhoPagina, lista);
	}

}
