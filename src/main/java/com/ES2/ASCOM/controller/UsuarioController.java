package com.ES2.ASCOM.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ES2.ASCOM.model.Grupo;
import com.ES2.ASCOM.model.Usuario;
import com.ES2.ASCOM.repository.CustomUsuarioDAO;
import com.ES2.ASCOM.repository.GrupoDAO;
import com.ES2.ASCOM.repository.UsuarioDAO;
import com.ES2.ASCOM.exception.ApiRequestException;
import com.ES2.ASCOM.helpers.TokenService;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {
	
	
	@Autowired
	private UsuarioDAO usuarioDAO;
	
	@Autowired
	private GrupoDAO grupoDAO;
	
	@Autowired
	private CustomUsuarioDAO customUsuarioDAO;
	
	private TokenService tokenService = new TokenService();
	PasswordEncoder encoder;
	
	@GetMapping("/teste")
	public String teste() {
		return "Teste bem sucedidio";
	}
	
	@GetMapping("/listar")
	public Map<String,Object> listar(@PageableDefault(value = 2) Pageable pageable, @RequestHeader Map<String,String> header,
								@RequestParam(required = false) String nome, @RequestParam(required = false) String email,
								@RequestParam(required = false) String profissao, @RequestParam(required = false) String ativo,
								@RequestParam(required = false) Integer grupo_id, @RequestParam(required = false) Integer paginaAtual,
								@RequestParam(required = false) Integer tamanhoPagina) {
		
		
		String token = header.get("token");
		if(token == null)
			throw new ApiRequestException("Token não foi informado",HttpStatus.BAD_REQUEST);
		
		Integer user_id = tokenService.getTokenSubject(token);
		
		if( ativo != null && (!ativo.equals("true") && !ativo.equals("false")) ) 
			throw new ApiRequestException("Ativo deve receber true ou false",HttpStatus.BAD_REQUEST);
		
		if(grupo_id != null) {
			Optional<Grupo> group = grupoDAO.findById(grupo_id);
			if(!group.isPresent()) 
				throw new ApiRequestException("Não existe um grupo com id = "+grupo_id,HttpStatus.BAD_REQUEST);
		}
		
		Map<String,Object> lista = customUsuarioDAO.listagemCustomizada(nome, email, profissao, ativo, grupo_id, paginaAtual, tamanhoPagina);
		return lista;
	}
	
	@PostMapping("/salvar")
	public Usuario salvar(@RequestBody Usuario user) {
		this.encoder = new BCryptPasswordEncoder();
		String encodePassword = this.encoder.encode(user.getSenha());
		user.setSenha(encodePassword);
		return usuarioDAO.save(user);
	}
	
	@PostMapping("/login")
	public Map<String,String> login(@RequestBody Map<String,String> json) {
		String email = json.get("email");
		String senha = json.get("senha");
		Optional<Usuario> user = usuarioDAO.findByEmail(email);
		if(!user.isPresent()) 
			throw new ApiRequestException("Não existe usuario com este email",HttpStatus.FORBIDDEN);
			
		Usuario usuario = user.get();
		
		String senha_correta = user.get().getSenha();
		if(!senha_correta.equals(senha))
			throw new ApiRequestException("Senha está incorreta",HttpStatus.FORBIDDEN);
		
		Map<String,String> result = new HashMap<String,String>();
		result.put("token",tokenService.generateToken(usuario));
		return result;
	}
	
	/**
	 * usario/  => 
	 * usuario/salvar => POST
	 * usuario/login => POST
	 * usuario/alterar => POST
	 * usuario/listar => GET
	 * usuario/desabilitar => POST
	 * usuario/busca/{value} => GET
	 * */

	/*
	 * ExampleMatcher matcher = ExampleMatcher .matchingAll() .withMatcher("email",
	 * GenericPropertyMatchers.contains().ignoreCase()) .withMatcher("nome",
	 * GenericPropertyMatchers.contains().ignoreCase()) .withMatcher("profissao",
	 * GenericPropertyMatchers.contains().ignoreCase());
	 * 
	 * 
	 * Usuario user = new Usuario(null, email, nome,null, profissao, ativo, null);
	 * Example.of(user,matcher)
	 */
}
