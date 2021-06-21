package com.ES2.ASCOM.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ES2.ASCOM.model.Grupo;
import com.ES2.ASCOM.model.Permissao;
import com.ES2.ASCOM.model.Usuario;
import com.ES2.ASCOM.repository.CustomUsuarioDAO;
import com.ES2.ASCOM.repository.GrupoDAO;
import com.ES2.ASCOM.repository.PermissaoDAO;
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
	private PermissaoDAO permissaoDAO;
	@Autowired
	private CustomUsuarioDAO customUsuarioDAO;
	private TokenService tokenService = new TokenService();
	PasswordEncoder encoder;

	@GetMapping("/teste")
	public List<Permissao> teste() {

		return permissaoDAO.permissoesGrupo(2);
		// return null;
	}
	
	@PutMapping("{id}/mudarDeGrupo")
	public Map<String, String> alterarGrupo(@PathVariable Integer id ,@RequestHeader Map<String, String> header, @RequestBody Map<String, String> json) {
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		Integer grupo_id;
		try {
			grupo_id = Integer.valueOf(json.get("grupo_id"));
		} catch (NumberFormatException e) {
			throw new ApiRequestException("Informe um grupo_id,sendo que ele deve ser um numero inteiro positivo",HttpStatus.BAD_REQUEST);
		}
		
		Optional<Grupo> groupAux = grupoDAO.findById(grupo_id);
		if(groupAux.isEmpty()) throw new ApiRequestException("Grupo com id = "+grupo_id+" não existe", HttpStatus.BAD_REQUEST);

		Optional<Usuario> userAux = usuarioDAO.findById(id);
		if (userAux.isEmpty()) throw new ApiRequestException("Usuario com id = "+id+" não existe", HttpStatus.BAD_REQUEST);
		
		Usuario user = userAux.get();
		Grupo group = groupAux.get();
;		user.setGrupo(group);;
		usuarioDAO.save(user);
		
		Map<String, String> result = new HashMap<String, String>();
		result.put("message", "Grupo o qual o usuario pertence foi mudado com sucesso ");
		return result;
	}
	
	@PutMapping("{id}/alterarStatus")
	public Map<String, String> alterarStatus(@PathVariable Integer id ,@RequestHeader Map<String, String> header, @RequestBody Map<String, String> json) {
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		String ativo = json.get("ativo");
		if (ativo == null || (!ativo.equals("true") && !ativo.equals("false")))
			throw new ApiRequestException("Ativo deve receber true ou false", HttpStatus.BAD_REQUEST);
		

		Optional<Usuario> userAux = usuarioDAO.findById(id);
		if (!userAux.isPresent()) throw new ApiRequestException("Usuario com id = "+id+" não existe", HttpStatus.BAD_REQUEST);
		
		Usuario user = userAux.get();
		boolean active = Boolean.parseBoolean(ativo);
		user.setAtivo(active);
		usuarioDAO.save(user);
		
		Map<String, String> result = new HashMap<String, String>();
		result.put("message", "Status do usuario atualizado com sucesso ");
		return result;
	}

	@PostMapping("/login")
	public Map<String, String> login(@RequestBody Map<String, String> json) {
		String email = json.get("email");
		String senha = json.get("senha");
		Optional<Usuario> user = usuarioDAO.findByEmail(email);
		if (!user.isPresent())
			throw new ApiRequestException("Não existe usuario com este email", HttpStatus.FORBIDDEN);

		Usuario usuario = user.get();   
		
		if(!usuario.isAtivo())
			throw new ApiRequestException("O usuario com este email está inativo  ", HttpStatus.FORBIDDEN);
		
		String senha_correta = user.get().getSenha();
		if (!senha_correta.equals(senha))
			throw new ApiRequestException("Senha está incorreta", HttpStatus.FORBIDDEN);

		Map<String, String> result = new HashMap<String, String>();
		result.put("token", tokenService.generateToken(usuario));
		return result;
	}

	@GetMapping("/{id}")
	public Optional<Usuario> achar(@PathVariable Integer id, @RequestHeader Map<String,String> header) {
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		Optional<Usuario> user = usuarioDAO.findById(id);
		if (user.isPresent()) user.get().setSenha(null);
		else throw new ApiRequestException("Usuario com id = "+id+" não existe", HttpStatus.BAD_REQUEST);
		return user;
	}

	@PutMapping("/alterarMeusDados")
	public Map<String, String> mudarMeusDados(@RequestHeader Map<String, String> header, @RequestBody Map<String, String> json) {

		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);

		String nome = json.get("nome");
		String email = json.get("email");
		String senha_original = json.get("senha_original");
		String senha_nova = json.get("senha_nova");
		String senha_nova_confirmacao = json.get("senha_nova_confirmacao");
		String profissao = json.get("profissao");

		if (nome != null)
			logged_user.setNome(nome);
		if (profissao != null)
			logged_user.setProfissao(profissao);

		if (email != null && !logged_user.getEmail().equals(email)) {
			Optional<Usuario> usuario_email = usuarioDAO.findByEmail(email);
			if (usuario_email.isPresent())
				throw new ApiRequestException("Já existem um usuario com email = " + email, HttpStatus.BAD_REQUEST);
			logged_user.setEmail(email);
		}

		boolean senha_original_correta = logged_user.getSenha().equals(senha_original);
		if (senha_original != null) {
			if (!senha_original_correta)
				throw new ApiRequestException("A senha original está incorreta", HttpStatus.BAD_REQUEST);
			if (senha_nova == null || senha_nova_confirmacao == null)
				throw new ApiRequestException("Informe a nova senha e sua confirmacao", HttpStatus.BAD_REQUEST);
			if (!senha_nova.equals(senha_nova_confirmacao))
				throw new ApiRequestException("A nova senha e sua confirmacao são diferentes", HttpStatus.BAD_REQUEST);

			logged_user.setSenha(senha_nova);
		} else {
			if (senha_nova != null || senha_nova_confirmacao != null)
				throw new ApiRequestException(
						"Para mudar a senha é necessario informar: a senha original, a senha nova e a confirmacao dela",
						HttpStatus.BAD_REQUEST);
		}
		usuarioDAO.save(logged_user);
		
		Map<String, String> result = new HashMap<String, String>();
		result.put("message", "Dados atualizados com sucesso");
		return result;
	}

	@GetMapping("/listar")
	public Map<String, Object> listar(@RequestHeader Map<String, String> header,
			@RequestParam(required = false) String nome, @RequestParam(required = false) String email,
			@RequestParam(required = false) String profissao, @RequestParam(required = false) String ativo,
			@RequestParam(required = false) Integer grupo_id, @RequestParam(required = false) String ordenacao_nome,
			@RequestParam(required = false) Integer paginaAtual, @RequestParam(required = false) Integer tamanhoPagina) {

		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);

		if (ativo != null && (!ativo.equals("true") && !ativo.equals("false")))
			throw new ApiRequestException("Ativo deve receber true ou false", HttpStatus.BAD_REQUEST);
		
		if (ordenacao_nome != null && (!ordenacao_nome.equals("asc") && !ordenacao_nome.equals("desc")))
			throw new ApiRequestException("Ordenacao do nome deve receber asc ou desc", HttpStatus.BAD_REQUEST);

		if (grupo_id != null) {
			Optional<Grupo> group = grupoDAO.findById(grupo_id);
			if (!group.isPresent())
				throw new ApiRequestException("Não existe um grupo com id = " + grupo_id, HttpStatus.BAD_REQUEST);
		}

		Map<String, Object> lista = customUsuarioDAO.listagemCustomizada(nome, email, profissao, ativo, grupo_id,
																		ordenacao_nome, paginaAtual, tamanhoPagina);
		return lista;
	}

	@PostMapping("/salvar")
	public Map<String, Integer> salvar(@RequestHeader Map<String, String> header,
			@RequestBody Map<String, String> json) {

		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);

		String nome = json.get("nome");
		String email = json.get("email");
		String senha = json.get("senha");
		String profissao = json.get("profissao");
		Integer grupo_id;

		try {
			grupo_id = Integer.valueOf(json.get("grupo_id"));
		} catch (NumberFormatException e) {
			throw new ApiRequestException("Informe um grupo_id,sendo que ele deve ser um numero inteiro positivo",
					HttpStatus.BAD_REQUEST);
		}
		if (nome == null)
			throw new ApiRequestException("Informe um nome", HttpStatus.BAD_REQUEST);
		if (email == null)
			throw new ApiRequestException("Informe um email", HttpStatus.BAD_REQUEST);
		if (email == senha)
			throw new ApiRequestException("Informe uma senha", HttpStatus.BAD_REQUEST);
		if (profissao == null)
			throw new ApiRequestException("Informe uma profissao", HttpStatus.BAD_REQUEST);

		Optional<Grupo> group = grupoDAO.findById(grupo_id);
		if (!group.isPresent())
			throw new ApiRequestException("Não existe um grupo com id = " + grupo_id, HttpStatus.BAD_REQUEST);

		Optional<Usuario> usuario_email = usuarioDAO.findByEmail(email);
		if (usuario_email.isPresent())
			throw new ApiRequestException("Já existem um usuario com email = " + email, HttpStatus.BAD_REQUEST);

		Usuario user = new Usuario(null, email, nome, senha, profissao, true, group.get());
		user = usuarioDAO.save(user);
		
		Map<String, Integer> result = new HashMap<String, Integer>();
		result.put("id", user.getId());
		return result;
		// this.encoder = new BCryptPasswordEncoder();

		// String encodePassword = this.encoder.encode(user.getSenha());
		// user.setSenha(encodePassword);

	}

	/**
	 * usario/ => usuario/salvar => POST usuario/login => POST usuario/alterar =>
	 * POST usuario/listar => GET usuario/desabilitar => POST usuario/busca/{value}
	 * => GET
	 */

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
