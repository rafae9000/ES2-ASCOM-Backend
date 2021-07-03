package com.ES2.ASCOM.controller;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import net.bytebuddy.utility.RandomString;

import com.ES2.ASCOM.model.Chamado;
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
	@Autowired
	private JavaMailSender emailSender;
	private TokenService tokenService = new TokenService();
	
	private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private Validator validator = factory.getValidator();
	
	@PutMapping("{id}/mudarDeGrupo")
	public Map<String, String> alterarGrupo(@PathVariable Integer id ,@RequestHeader Map<String, String> header, @RequestBody Map<String, String> json) throws ApiRequestException {
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.getGrupo().getNome().contentEquals("administrador"))
			throw new ApiRequestException("Somente um administrador pode mudar o grupo que um usuario pertence", HttpStatus.FORBIDDEN);
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		Integer grupo_id;
		try {
			grupo_id = Integer.valueOf(json.get("grupo_id"));
		} catch (NumberFormatException e) {
			throw new ApiRequestException("Informe um grupo_id,sendo que ele deve ser um numero inteiro positivo",HttpStatus.BAD_REQUEST);
		}
		
		Optional<Grupo> groupAux = grupoDAO.findById(grupo_id);
		if(!groupAux.isPresent()) throw new ApiRequestException("Grupo com id = "+grupo_id+" não existe", HttpStatus.BAD_REQUEST);

		Optional<Usuario> userAux = usuarioDAO.findById(id);
		if (!userAux.isPresent()) throw new ApiRequestException("Usuario com id = "+id+" não existe", HttpStatus.BAD_REQUEST);
		
		Usuario user = userAux.get();
		Grupo group = groupAux.get();
;		user.setGrupo(group);;
		usuarioDAO.save(user);
		
		Map<String, String> result = new HashMap<String, String>();
		result.put("message", "Grupo o qual o usuario pertence foi mudado com sucesso ");
		return result;
	}
	
	@PutMapping("{id}/alterarStatus")
	public Map<String, String> alterarStatus(@PathVariable Integer id ,@RequestHeader Map<String, String> header, @RequestBody Map<String, String> json) throws ApiRequestException {
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		if(!logged_user.getGrupo().getNome().contentEquals("administrador"))
			throw new ApiRequestException("Somente um administrador pode mudar o status de um usuario", HttpStatus.FORBIDDEN);
		
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
	public Map<String, String> login(@RequestBody Map<String, String> json) throws ApiRequestException {
		String email = json.get("email");
		String senha = json.get("senha");
		Optional<Usuario> user = usuarioDAO.findByEmail(email);
		if (!user.isPresent())
			throw new ApiRequestException("Não existe usuario com este email", HttpStatus.FORBIDDEN);

		Usuario usuario = user.get();   
		
		if(!usuario.isAtivo())
			throw new ApiRequestException("O usuario com este email está inativo  ", HttpStatus.FORBIDDEN);
		
		String senha_correta = user.get().getSenha();
		if (!BCrypt.checkpw(senha, senha_correta))
			throw new ApiRequestException("Senha está incorreta", HttpStatus.FORBIDDEN);

		Map<String, String> result = new HashMap<String, String>();
		result.put("token", tokenService.generateToken(usuario));
		result.put("grupo", usuario.getGrupo().getNome());
		return result;
	}

	@GetMapping("/{id}")
	public Optional<Usuario> achar(@PathVariable Integer id, @RequestHeader Map<String,String> header) throws ApiRequestException {
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
	
	@GetMapping("/meusDados")
	public Optional<Usuario> pegarMeusDados(@RequestHeader Map<String,String> header) throws ApiRequestException {
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		Optional<Usuario> user = usuarioDAO.findById(logged_user_id);
		user.get().setSenha(null);
		return user;
	}

	@PutMapping("/alterarMeusDados")
	public Map<String, String> mudarMeusDados(@RequestHeader Map<String, String> header, @RequestBody Map<String, String> json) throws ApiRequestException {

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
			if(!email.endsWith("@ebserh.gov.br"))
				throw new ApiRequestException("O novo email deve pertencer ao dominio @ebserh.gov.br", HttpStatus.BAD_REQUEST);
			Optional<Usuario> usuario_email = usuarioDAO.findByEmail(email);
			if (usuario_email.isPresent())
				throw new ApiRequestException("Já existem um usuario com email = " + email, HttpStatus.BAD_REQUEST);
			logged_user.setEmail(email);
		}
		
        String senha_correta = logged_user.getSenha();
		if (senha_original != null) {
			if (!BCrypt.checkpw(senha_original, senha_correta))
				throw new ApiRequestException("A senha original está incorreta", HttpStatus.BAD_REQUEST);
			if (senha_nova == null || senha_nova_confirmacao == null)
				throw new ApiRequestException("Informe a nova senha e sua confirmacao", HttpStatus.BAD_REQUEST);
			if(senha_nova.length() < 6)
				throw new ApiRequestException("Tamanho minimo da senha nova deve ser de 6 caracteres", HttpStatus.BAD_REQUEST);
			if (!senha_nova.equals(senha_nova_confirmacao))
				throw new ApiRequestException("A nova senha e sua confirmacao são diferentes", HttpStatus.BAD_REQUEST);
			
			senha_nova =  BCrypt.hashpw(senha_nova, BCrypt.gensalt());
			logged_user.setSenha(senha_nova);
		} else {
			if (senha_nova != null || senha_nova_confirmacao != null)
				throw new ApiRequestException(
						"Para mudar a senha é necessario informar: a senha original, a senha nova e a confirmacao dela",
						HttpStatus.BAD_REQUEST);
		}
		
		Set<ConstraintViolation<Usuario>> violations = validator.validate(logged_user);
		for (ConstraintViolation<Usuario> violation : violations) {
			throw new ApiRequestException(violation.getMessage(), HttpStatus.BAD_REQUEST);
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
			@RequestParam(required = false) Integer paginaAtual, @RequestParam(required = false) Integer tamanhoPagina) throws ApiRequestException {

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
			@RequestBody Map<String, String> json) throws ApiRequestException {

		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		if(!logged_user.getGrupo().getNome().contentEquals("administrador"))
			throw new ApiRequestException("Somente um administrador pode criar um novo usuario", HttpStatus.FORBIDDEN);

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

		if (email == null)
			throw new ApiRequestException("Informe um email", HttpStatus.BAD_REQUEST);
		if (!email.endsWith("@ebserh.gov.br"))
			throw new ApiRequestException("O email deve pertencer ao dominio @ebserh.gov.br", HttpStatus.BAD_REQUEST);
		if (senha == null)
			throw new ApiRequestException("Informe uma senha", HttpStatus.BAD_REQUEST);
		if(senha.length() < 6)
			throw new ApiRequestException("Tamanho minimo da senha deve ser de 6 caracteres", HttpStatus.BAD_REQUEST);


		Optional<Grupo> group = grupoDAO.findById(grupo_id);
		if (!group.isPresent())
			throw new ApiRequestException("Não existe um grupo com id = " + grupo_id, HttpStatus.BAD_REQUEST);

		Optional<Usuario> usuario_email = usuarioDAO.findByEmail(email);
		if (usuario_email.isPresent())
			throw new ApiRequestException("Já existem um usuario com email = " + email, HttpStatus.BAD_REQUEST);

		senha =  BCrypt.hashpw(senha, BCrypt.gensalt()); 
		Usuario user = new Usuario(null, email, nome, senha, profissao, true, null, group.get(),null,null);
		
		Set<ConstraintViolation<Usuario>> violations = validator.validate(user);
		for (ConstraintViolation<Usuario> violation : violations) {
			throw new ApiRequestException(violation.getMessage(), HttpStatus.BAD_REQUEST);
		}
		
		user = usuarioDAO.save(user);
		
		Map<String, Integer> result = new HashMap<String, Integer>();
		result.put("id", user.getId());
		return result;
		// this.encoder = new BCryptPasswordEncoder();

		// String encodePassword = this.encoder.encode(user.getSenha());
		// user.setSenha(encodePassword);

	}

	@PostMapping("/esqueceuSenha")
	public Map<String, String> esqueceuSenha(@RequestBody Map<String, String> json) throws ApiRequestException, UnsupportedEncodingException, MessagingException {
		String email = json.get("email");
		String url = json.get("url");
		if (email == null) throw new ApiRequestException("Email não foi informado",HttpStatus.BAD_REQUEST);
		if (url == null) throw new ApiRequestException("Url não foi informado",HttpStatus.BAD_REQUEST);
		
		Optional<Usuario> user = usuarioDAO.findByEmail(email);
		if(!user.isPresent()) throw new ApiRequestException("Não existe usuario com este email", HttpStatus.BAD_REQUEST);
		
		Usuario usuario = user.get();
		String tokenReset = RandomString.make(45);
		usuario.setTokenResetaSenha(tokenReset);
		usuarioDAO.save(usuario);
		
		String link = url+"?token="+tokenReset; 
		
		MimeMessage mensagem = emailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mensagem);
		helper.setTo(email); 
		String assunto = "Link para reiniciar";
		String conteudo = "<p>Olá,</p>"
				+ "<p>Você fez uma requisicao para reiniciar sua senha</p>"
				+ "<p>Aperte o link abaixo para mudar a sua senha</p>"
				+ "<p><b><a href=\""+link+"\">Mudar a senha</a><b></p>";
		
		helper.setSubject(assunto);
		helper.setText(conteudo,true);
	
		try {
			emailSender.send(mensagem);
		} catch (MailSendException e) {
			throw new ApiRequestException("Ocorreu uma falha ao enviar o email de recuperação", HttpStatus.BAD_GATEWAY);
		}
		
		Map<String, String> result = new HashMap<String, String>();
		result.put("message", "Foi enviado para seu email um link para alterar sua senha");
		
		return result;
	}
	
	@PostMapping("/alterarSenhaEsquecida")
	public Map<String, String> alterarSenhaEsquecida(@RequestBody Map<String, String> json) throws ApiRequestException{
		String senha_nova = json.get("senha_nova");
		String senha_nova_confirmacao = json.get("senha_nova_confirmacao");
		String token_alteracao = json.get("token_alteracao");
		
		if(senha_nova == null) throw new ApiRequestException("Senha nova não foi informada",HttpStatus.BAD_REQUEST);
		if(senha_nova.length() < 6) throw new ApiRequestException("Tamanho minimo da senha nova deve ser de 6 caracteres", HttpStatus.BAD_REQUEST);
		if(senha_nova_confirmacao == null) throw new ApiRequestException("Confirmação da senha não foi informada",HttpStatus.BAD_REQUEST);
		if(token_alteracao == null) throw new ApiRequestException("Token de alteração não foi informado",HttpStatus.BAD_REQUEST);
		
		if(!senha_nova.equals(senha_nova_confirmacao)) throw new ApiRequestException("Senha nova e sua confirmação não são iguais",HttpStatus.BAD_REQUEST);
		
		Optional<Usuario> user = usuarioDAO.findByTokenResetaSenha(token_alteracao);
		if(!user.isPresent())
			throw new ApiRequestException("Esse link de alteração ja foi utilizado,caso não tenha sido você entre em contato com o suporte tecnico",HttpStatus.UNAUTHORIZED);
		
		Usuario usuario = user.get();
		senha_nova =  BCrypt.hashpw(senha_nova, BCrypt.gensalt());
		usuario.setSenha(senha_nova);
		usuario.setTokenResetaSenha(null);
		usuarioDAO.save(usuario);
		
		Map<String, String> result = new HashMap<String, String>();
		result.put("message","Alteração da senha foi feita com sucesso");
		return result;	
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
