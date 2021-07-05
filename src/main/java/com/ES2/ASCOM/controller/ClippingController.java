package com.ES2.ASCOM.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ES2.ASCOM.enums.Feedback;
import com.ES2.ASCOM.enums.Noticia_portal_local;
import com.ES2.ASCOM.enums.Status_clipping;
import com.ES2.ASCOM.exception.ApiRequestException;
import com.ES2.ASCOM.helpers.TokenService;
import com.ES2.ASCOM.model.Chamado;
import com.ES2.ASCOM.model.Clipping;
import com.ES2.ASCOM.model.GeneroJornalistico;
import com.ES2.ASCOM.model.Grupo;
import com.ES2.ASCOM.model.Usuario;
import com.ES2.ASCOM.pagination.Paginacao;
import com.ES2.ASCOM.repository.ArquivoClippingDAO;
import com.ES2.ASCOM.repository.ChamadoDAO;
import com.ES2.ASCOM.repository.ClippingDAO;
import com.ES2.ASCOM.repository.CustomClippingDAO;
import com.ES2.ASCOM.repository.GeneroJornalisticoDAO;
import com.ES2.ASCOM.repository.UsuarioDAO;

@RestController
@RequestMapping("/clipping")
public class ClippingController {

	@Autowired
	private ArquivoClippingDAO arquivoClipingDAO;
	@Autowired
	private ClippingDAO clippingDAO;
	@Autowired 
	private CustomClippingDAO customClippingDAO;
	@Autowired
	private UsuarioDAO usuarioDAO;
	@Autowired
	private GeneroJornalisticoDAO generoJornalisticoDAO;
	@Autowired
	private ChamadoDAO chamadoDAO;
	
	private TokenService tokenService = new TokenService();;
	private final Paginacao<Clipping> pag = new Paginacao<Clipping>();
	
	private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private Validator validator = factory.getValidator();
	
	@PostMapping("/salvarClippingIndependente")
	public Map<String,Object> salvarClippingIndependente(@RequestHeader Map<String, String> header, @RequestBody Map<String, String> json) throws ApiRequestException{
		
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		//String genero_jornalistico_id = json.get("genero_jornalistico_id");
		//String usuario_atribuido_id =  json.get("usuario_atribuido_id");
		String titulo_materia = json.get("titulo_materia");
		String manteve_titulo = json.get("manteve_titulo");
		String subtitulo_materia = json.get("subtitulo_materia");
		String manteve_subtitulo = json.get("manteve_subtitulo");
		String editora = json.get("editora");
		String texto_integral = json.get("texto_integral");
		String manteve_texto = json.get("manteve_texto");
		String manifestacao_credito = json.get("manifestacao_credito");
		String classificacao = json.get("classificacao");
		String subclassificacao = json.get("subclassificacao");
	    String pessoas_citadas = json.get("pessoas_citadas");
		String url = json.get("url");
		String observacoes = json.get("observacoes");
		String feedback = json.get("feedback");
		String noticia_portal_local = json.get("noticia_portal_local");
		
		Status_clipping status = Status_clipping.aberto;
		Feedback feedback_enum = feedbackValido(feedback);
		Noticia_portal_local noticia_portal_local_enum = noticiaPortalValido(noticia_portal_local);
		
		if(feedback_enum == null) throw new ApiRequestException("Feedback deve receber positivo,negativo ou neutro", HttpStatus.BAD_REQUEST);
		if(noticia_portal_local_enum == null) throw new ApiRequestException("Noticia do portal local de receber sim ou nao", HttpStatus.BAD_REQUEST);
		
		Integer genero_jornalistico_id;
		try {
			genero_jornalistico_id = Integer.valueOf(json.get("genero_jornalistico_id"));
		} catch (NumberFormatException e) {
			throw new ApiRequestException("Informe um genero_jornalistico_id,sendo que ele deve ser um numero inteiro positivo",HttpStatus.BAD_REQUEST);
		}
		
		Optional<GeneroJornalistico> generoAux = generoJornalisticoDAO.findById(genero_jornalistico_id);
		if(!generoAux.isPresent()) throw new ApiRequestException("Gênero jornalistico com id = "+genero_jornalistico_id+" não existe", HttpStatus.BAD_REQUEST);
		GeneroJornalistico genero = generoAux.get();
		
		Integer usuario_atribuido_id = null;
		Usuario usuario_atribuido = null;

		try {		
			String aux = json.get("usuario_atribuido_id");
			if(aux != null) {
				usuario_atribuido_id = Integer.valueOf(json.get("usuario_atribuido_id"));
				Optional<Usuario> user = usuarioDAO.findById(usuario_atribuido_id );
				if(!user.isPresent())
					 throw new ApiRequestException("Não existe um usuario com o id = "+usuario_atribuido_id, HttpStatus.BAD_REQUEST);
				
				usuario_atribuido = user.get();	
			}		
		} catch (NumberFormatException e) {
			throw new ApiRequestException("Id do usuario atribuido deve ser um numero inteiro positivo ou null",HttpStatus.BAD_REQUEST);
		}

		LocalDate data_publicacao = LocalDate.now();
		Clipping clipping = new Clipping(null, logged_user, usuario_atribuido, null, genero, data_publicacao, noticia_portal_local_enum, 
									     titulo_materia, manteve_titulo, subtitulo_materia, manteve_subtitulo, editora, texto_integral, 
									     manteve_texto, manifestacao_credito, classificacao, subclassificacao, pessoas_citadas, url, 
									     observacoes, status, feedback_enum, null, null);
		
		Set<ConstraintViolation<Clipping>> violations = validator.validate(clipping);
		for (ConstraintViolation<Clipping> violation : violations) {
			throw new ApiRequestException(violation.getMessage(), HttpStatus.BAD_REQUEST);
		}
		clipping = clippingDAO.save(clipping);
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("message", "Clipping independente criado com sucesso");
		result.put("id",clipping.getId());
		return result;
	}
	
	@PostMapping("/salvarClippingVinculado")
	public Map<String,Object> salvarClippingVinculado(@RequestHeader Map<String, String> header, @RequestBody Map<String, String> json) throws ApiRequestException{
		
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		//String chamado_id = json.get("chamado_id");
		//String genero_jornalistico_id = json.get("genero_jornalistico_id");
		//String usuario_atribuido_id =  json.get("usuario_atribuido_id");
		String titulo_materia = json.get("titulo_materia");
		String manteve_titulo = json.get("manteve_titulo");
		String subtitulo_materia = json.get("subtitulo_materia");
		String manteve_subtitulo = json.get("manteve_subtitulo");
		String editora = json.get("editora");
		String texto_integral = json.get("texto_integral");
		String manteve_texto = json.get("manteve_texto");
		String manifestacao_credito = json.get("manifestacao_credito");
		String classificacao = json.get("classificacao");
		String subclassificacao = json.get("subclassificacao");
	    String pessoas_citadas = json.get("pessoas_citadas");
		String url = json.get("url");
		String observacoes = json.get("observacoes");
		String feedback = json.get("feedback");
		String noticia_portal_local = json.get("noticia_portal_local");
		
		Status_clipping status = Status_clipping.aberto;
		Feedback feedback_enum = feedbackValido(feedback);
		Noticia_portal_local noticia_portal_local_enum = noticiaPortalValido(noticia_portal_local);
		
		if(feedback_enum == null) throw new ApiRequestException("Feedback deve receber positivo,negativo ou neutro", HttpStatus.BAD_REQUEST);
		if(noticia_portal_local_enum == null) throw new ApiRequestException("Noticia do portal local de receber sim ou nao", HttpStatus.BAD_REQUEST);
		
		Integer genero_jornalistico_id;
		try {
			genero_jornalistico_id = Integer.valueOf(json.get("genero_jornalistico_id"));
		} catch (NumberFormatException e) {
			throw new ApiRequestException("Informe um genero_jornalistico_id,sendo que ele deve ser um numero inteiro positivo",HttpStatus.BAD_REQUEST);
		}
		
		Optional<GeneroJornalistico> generoAux = generoJornalisticoDAO.findById(genero_jornalistico_id);
		if(!generoAux.isPresent()) throw new ApiRequestException("Gênero jornalistico com id = "+genero_jornalistico_id+" não existe", HttpStatus.BAD_REQUEST);
		GeneroJornalistico genero = generoAux.get();
		
		Integer usuario_atribuido_id = null;
		Usuario usuario_atribuido = null;

		try {		
			String aux = json.get("usuario_atribuido_id");
			if(aux != null) {
				usuario_atribuido_id = Integer.valueOf(aux);
				Optional<Usuario> user = usuarioDAO.findById(usuario_atribuido_id );
				if(!user.isPresent())
					 throw new ApiRequestException("Não existe um usuario com o id = "+usuario_atribuido_id, HttpStatus.BAD_REQUEST);
				
				usuario_atribuido = user.get();	
			}		
		} catch (NumberFormatException e) {
			throw new ApiRequestException("Id do usuario atribuido deve ser um numero inteiro positivo ou null",HttpStatus.BAD_REQUEST);
		}
		
		Integer chamado_id = null;
		Chamado chamado = null;
		try {
			String aux = json.get("chamado_id");
			chamado_id = Integer.valueOf(aux);
			Optional<Chamado> chamadoAux = chamadoDAO.findById(chamado_id);
			if(!chamadoAux.isPresent())
				 throw new ApiRequestException("Não existe um chamado com o id = "+chamado_id, HttpStatus.BAD_REQUEST);
			
			chamado = chamadoAux.get();
			Optional<Clipping> clippingVinculado = clippingDAO.findClippingBindWithChamado(chamado.getId());
			if(clippingVinculado.isPresent())
				 throw new ApiRequestException("chamado com o id = "+chamado_id+" já está vinculado à um clipping", HttpStatus.BAD_REQUEST);
			
		} catch (NumberFormatException e) {
			throw new ApiRequestException("Informe um chamado_id,sendo que ele deve ser um numero inteiro positivo",HttpStatus.BAD_REQUEST);
		}

		LocalDate data_publicacao = LocalDate.now();
		Clipping clipping = new Clipping(null, logged_user, usuario_atribuido, chamado, genero, data_publicacao, noticia_portal_local_enum, 
									     titulo_materia, manteve_titulo, subtitulo_materia, manteve_subtitulo, editora, texto_integral, 
									     manteve_texto, manifestacao_credito, classificacao, subclassificacao, pessoas_citadas, url, 
									     observacoes, status, feedback_enum, null, null);
		
		Set<ConstraintViolation<Clipping>> violations = validator.validate(clipping);
		for (ConstraintViolation<Clipping> violation : violations) {
			throw new ApiRequestException(violation.getMessage(), HttpStatus.BAD_REQUEST);
		}
		clipping = clippingDAO.save(clipping);
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("message", "Clipping vinculado criado com sucesso");
		result.put("id",clipping.getId());
		return result;
	}
	
	@GetMapping("/{id}")
	public Clipping achar(@PathVariable Integer id, @RequestHeader Map<String, String> header) throws ApiRequestException {
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		Optional<Clipping> aux = clippingDAO.findById(id);
		if(!aux.isPresent())
			throw new ApiRequestException("Não existe clipping com id = "+id, HttpStatus.BAD_REQUEST);
		
		Clipping clipping = aux.get();
		
		return clipping;
	}
	
	@PutMapping("{id}/atribuir")
	public Map<String,String> atribuir(@PathVariable Integer id, @RequestHeader Map<String, String> header, @RequestBody Map<String, String> json) 
			throws ApiRequestException{
		
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		Optional<Clipping> aux = clippingDAO.findById(id);
		if(!aux.isPresent())
			throw new ApiRequestException("Não existe um clipping com id = "+id, HttpStatus.BAD_REQUEST);
		
		Clipping clipping = aux.get();
		Integer usuario_atribuido_id = null;

		try {	
			String ajudante = json.get("usuario_atribuido_id");
			if(ajudante != null)
				usuario_atribuido_id = Integer.valueOf(json.get("usuario_atribuido_id"));
		} catch (NumberFormatException e) {
			throw new ApiRequestException("Id do usuario atribuido deve ser um numero inteiro positivo ou null",HttpStatus.BAD_REQUEST);
		}
		
		Usuario usuario_atribuido = null;
		if(usuario_atribuido_id != null) {
			Optional<Usuario> user = usuarioDAO.findById(usuario_atribuido_id );
			if(!user.isPresent())
				 throw new ApiRequestException("Não existe um usuario com o id = "+usuario_atribuido_id, HttpStatus.BAD_REQUEST);
				
			usuario_atribuido = user.get();
		}
		clipping.setUsuario_atribuido(usuario_atribuido);
		clippingDAO.save(clipping);
		
		Map<String, String> result = new HashMap<String, String>();
		result.put("message", "Clipping foi atribuido com sucesso");
		
		return result;
	}
	
	@PutMapping("{id}/avaliar")
	public Map<String,Object> avaliar(@PathVariable Integer id, @RequestHeader Map<String, String> header, @RequestBody Map<String, String> json) 
											throws ApiRequestException{
		
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		Grupo grupo = logged_user.getGrupo();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		if(!grupo.getNome().equals("administrador"))
			throw new ApiRequestException("Apenas um administrador pode avaliar um clipping", HttpStatus.FORBIDDEN);
		
		Optional<Clipping> clippingAux = clippingDAO.findById(id);
		if(!clippingAux.isPresent())
			throw new ApiRequestException("Não existe um clipping com id = "+id, HttpStatus.BAD_REQUEST);
		
		Clipping clipping = clippingAux.get();
		
		//String genero_jornalistico_id = json.get("genero_jornalistico_id");
		//String usuario_atribuido_id =  json.get("usuario_atribuido_id");
		String titulo_materia = json.get("titulo_materia");
		String manteve_titulo = json.get("manteve_titulo");
		String subtitulo_materia = json.get("subtitulo_materia");
		String manteve_subtitulo = json.get("manteve_subtitulo");
		String editora = json.get("editora");
		String texto_integral = json.get("texto_integral");
		String manteve_texto = json.get("manteve_texto");
		String manifestacao_credito = json.get("manifestacao_credito");
		String classificacao = json.get("classificacao");
		String subclassificacao = json.get("subclassificacao");
	    String pessoas_citadas = json.get("pessoas_citadas");
		String url = json.get("url");
		String observacoes = json.get("observacoes");
		String feedback = json.get("feedback");
		String noticia_portal_local = json.get("noticia_portal_local");
		String status = json.get("status");
		String justificativa = json.get("justificativa");
		
		
		Status_clipping status_enum = statusValido(status);
		Feedback feedback_enum = feedbackValido(feedback);
		Noticia_portal_local noticia_portal_local_enum = noticiaPortalValido(noticia_portal_local);
		
		if(feedback_enum == null) throw new ApiRequestException("Feedback deve receber positivo,negativo ou neutro", HttpStatus.BAD_REQUEST);
		if(noticia_portal_local_enum == null) throw new ApiRequestException("Noticia do portal local de receber sim ou nao", HttpStatus.BAD_REQUEST);
		if(status_enum == null) throw new ApiRequestException("Status deve receber aberto,fechado ou anulado", HttpStatus.BAD_REQUEST);
		if(justificativa == null || justificativa.trim().length() == 0) 
			throw new ApiRequestException("Justificativa inválida", HttpStatus.BAD_REQUEST);
		if(justificativa.length() > 250)
			throw new ApiRequestException("Tamanho da justificativa deve ser no maximo 250 caracteres", HttpStatus.BAD_REQUEST);
		
		Integer genero_jornalistico_id;
		try {
			genero_jornalistico_id = Integer.valueOf(json.get("genero_jornalistico_id"));
		} catch (NumberFormatException e) {
			throw new ApiRequestException("Informe um genero_jornalistico_id,sendo que ele deve ser um numero inteiro positivo",HttpStatus.BAD_REQUEST);
		}
		
		Optional<GeneroJornalistico> generoAux = generoJornalisticoDAO.findById(genero_jornalistico_id);
		if(!generoAux.isPresent()) throw new ApiRequestException("Gênero jornalistico com id = "+genero_jornalistico_id+" não existe", HttpStatus.BAD_REQUEST);
		GeneroJornalistico genero = generoAux.get();
		
		Integer usuario_atribuido_id = null;
		Usuario usuario_atribuido = null;

		try {		
			String aux = json.get("usuario_atribuido_id");
			if(aux != null) {
				usuario_atribuido_id = Integer.valueOf(json.get("usuario_atribuido_id"));
				Optional<Usuario> user = usuarioDAO.findById(usuario_atribuido_id );
				if(!user.isPresent())
					 throw new ApiRequestException("Não existe um usuario com o id = "+usuario_atribuido_id, HttpStatus.BAD_REQUEST);
				
				usuario_atribuido = user.get();	
			}		
		} catch (NumberFormatException e) {
			throw new ApiRequestException("Id do usuario atribuido deve ser um numero inteiro positivo ou null",HttpStatus.BAD_REQUEST);
		}
		
		clipping.setGenero_jornalistico(genero);
		clipping.setUsuario_atribuido(usuario_atribuido);
		clipping.setTitulo_materia(titulo_materia);
		clipping.setManteve_titulo(manteve_titulo);
		clipping.setSubtitulo_materia(subtitulo_materia);
		clipping.setManteve_subtitulo(manteve_subtitulo);
		clipping.setEditora(editora);
		clipping.setTexto_integral(texto_integral);
		clipping.setManteve_texto(manteve_texto);
		clipping.setManifestacao_credito(manifestacao_credito);
		clipping.setClassificacao(classificacao);
		clipping.setSubclassificacao(subclassificacao);
		clipping.setPessoas_citadas(pessoas_citadas);
		clipping.setUrl(url);
		clipping.setObservacoes(observacoes);
		clipping.setFeedback(feedback_enum);
		clipping.setNoticia_portal_local(noticia_portal_local_enum);
		clipping.setStatus(status_enum);
		clipping.setJustificativa(justificativa);
		
		Set<ConstraintViolation<Clipping>> violations = validator.validate(clipping);
		for (ConstraintViolation<Clipping> violation : violations) {
			throw new ApiRequestException(violation.getMessage(), HttpStatus.BAD_REQUEST);
		}
		clipping = clippingDAO.save(clipping);
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("message", "Clipping avaliado com sucesso");
		return result;
	}
	
	@PutMapping("{id}/alterar")
	public Map<String,Object> alterar(@PathVariable Integer id, @RequestHeader Map<String, String> header, @RequestBody Map<String, String> json) 
											throws ApiRequestException{
		
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		Grupo grupo = logged_user.getGrupo();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
	
		
		Optional<Clipping> clippingAux = clippingDAO.findById(id);
		if(!clippingAux.isPresent())
			throw new ApiRequestException("Não existe um clipping com id = "+id, HttpStatus.BAD_REQUEST);
		
		Clipping clipping = clippingAux.get();
		
		Integer criador_clipping_id = clipping.getUsuario().getId();
		if(!grupo.getNome().equals("administrador")) {
			if(!clipping.getStatus().toString().equals("aberto")) 
				throw new ApiRequestException("Somente um administrador pode alterar um clipping que não estão em aberto", HttpStatus.FORBIDDEN);
			if(logged_user_id != criador_clipping_id)
				throw new ApiRequestException("Somente um administrador ou o criador deste clipping pode alterar os dados", HttpStatus.FORBIDDEN);
			
		}
		
		//String genero_jornalistico_id = json.get("genero_jornalistico_id");
		//String usuario_atribuido_id =  json.get("usuario_atribuido_id");
		String titulo_materia = json.get("titulo_materia");
		String manteve_titulo = json.get("manteve_titulo");
		String subtitulo_materia = json.get("subtitulo_materia");
		String manteve_subtitulo = json.get("manteve_subtitulo");
		String editora = json.get("editora");
		String texto_integral = json.get("texto_integral");
		String manteve_texto = json.get("manteve_texto");
		String manifestacao_credito = json.get("manifestacao_credito");
		String classificacao = json.get("classificacao");
		String subclassificacao = json.get("subclassificacao");
	    String pessoas_citadas = json.get("pessoas_citadas");
		String url = json.get("url");
		String observacoes = json.get("observacoes");
		String feedback = json.get("feedback");
		String noticia_portal_local = json.get("noticia_portal_local");
		String justificativa = json.get("justificativa");
		
		Feedback feedback_enum = feedbackValido(feedback);
		Noticia_portal_local noticia_portal_local_enum = noticiaPortalValido(noticia_portal_local);
		
		if(feedback_enum == null) throw new ApiRequestException("Feedback deve receber positivo,negativo ou neutro", HttpStatus.BAD_REQUEST);
		if(noticia_portal_local_enum == null) throw new ApiRequestException("Noticia do portal local de receber sim ou nao", HttpStatus.BAD_REQUEST);
		if(justificativa == null || justificativa.trim().length() == 0) 
			throw new ApiRequestException("Justificativa inválida", HttpStatus.BAD_REQUEST);
		if(justificativa.length() > 250)
			throw new ApiRequestException("Tamanho da justificativa deve ser no maximo 250 caracteres", HttpStatus.BAD_REQUEST);
		
		Integer genero_jornalistico_id;
		try {
			genero_jornalistico_id = Integer.valueOf(json.get("genero_jornalistico_id"));
		} catch (NumberFormatException e) {
			throw new ApiRequestException("Informe um genero_jornalistico_id,sendo que ele deve ser um numero inteiro positivo",HttpStatus.BAD_REQUEST);
		}
		
		Optional<GeneroJornalistico> generoAux = generoJornalisticoDAO.findById(genero_jornalistico_id);
		if(!generoAux.isPresent()) throw new ApiRequestException("Gênero jornalistico com id = "+genero_jornalistico_id+" não existe", HttpStatus.BAD_REQUEST);
		GeneroJornalistico genero = generoAux.get();
		
		Integer usuario_atribuido_id = null;
		Usuario usuario_atribuido = null;

		try {		
			String aux = json.get("usuario_atribuido_id");
			if(aux != null) {
				usuario_atribuido_id = Integer.valueOf(json.get("usuario_atribuido_id"));
				Optional<Usuario> user = usuarioDAO.findById(usuario_atribuido_id );
				if(!user.isPresent())
					 throw new ApiRequestException("Não existe um usuario com o id = "+usuario_atribuido_id, HttpStatus.BAD_REQUEST);
				
				usuario_atribuido = user.get();	
			}		
		} catch (NumberFormatException e) {
			throw new ApiRequestException("Id do usuario atribuido deve ser um numero inteiro positivo ou null",HttpStatus.BAD_REQUEST);
		}
		
		clipping.setGenero_jornalistico(genero);
		clipping.setUsuario_atribuido(usuario_atribuido);
		clipping.setTitulo_materia(titulo_materia);
		clipping.setManteve_titulo(manteve_titulo);
		clipping.setSubtitulo_materia(subtitulo_materia);
		clipping.setManteve_subtitulo(manteve_subtitulo);
		clipping.setEditora(editora);
		clipping.setTexto_integral(texto_integral);
		clipping.setManteve_texto(manteve_texto);
		clipping.setManifestacao_credito(manifestacao_credito);
		clipping.setClassificacao(classificacao);
		clipping.setSubclassificacao(subclassificacao);
		clipping.setPessoas_citadas(pessoas_citadas);
		clipping.setUrl(url);
		clipping.setObservacoes(observacoes);
		clipping.setFeedback(feedback_enum);
		clipping.setNoticia_portal_local(noticia_portal_local_enum);
		clipping.setJustificativa(justificativa);
		
		Set<ConstraintViolation<Clipping>> violations = validator.validate(clipping);
		for (ConstraintViolation<Clipping> violation : violations) {
			throw new ApiRequestException(violation.getMessage(), HttpStatus.BAD_REQUEST);
		}
		clipping = clippingDAO.save(clipping);
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("message", "Clipping alterado com sucesso");
		return result;
	}

	@PostMapping("/{id}/adicionarArquivo")
	public Map<String,String> addArquivo(@PathVariable Integer id, @RequestParam("arquivo") MultipartFile multipartFile)
			throws IOException, ApiRequestException {
        
		Optional<Clipping> aux = clippingDAO.findById(id);
		if(!aux.isPresent())
			throw new ApiRequestException("Não existe clipping com id = "+id, HttpStatus.BAD_REQUEST);
		
		String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
		String uploadDir = "clipping-arquivos/" + id; 
		saveFile(uploadDir, fileName, multipartFile);
        // procurar o codigo para achar o path de onde o jar esta
        
		return null;
   }

	@GetMapping("/listarClippingsAbertosSemAtribuicao")
	public Map<String,Object> listarChamadosAbertosSemAtribuicao(@RequestHeader Map<String,String> header, 
																 @RequestParam(required = false) Integer paginaAtual, 
																 @RequestParam(required = false) Integer tamanhoPagina) throws ApiRequestException{
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		List<Clipping> lista = clippingDAO.clippingsAbertosSemAtribuicao();
		return pag.paginarLista(paginaAtual, tamanhoPagina, lista);
	}
	
	@GetMapping("/listarClippingsAbertosAtribuidosParaMim")
	public Map<String,Object> listarChamadosAbertosParaMim(@RequestHeader Map<String,String> header, 
																 @RequestParam(required = false) Integer paginaAtual, 
																 @RequestParam(required = false) Integer tamanhoPagina) throws ApiRequestException{
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		List<Clipping> lista = clippingDAO.clippingsAbertoAtribuido(logged_user_id);
		return pag.paginarLista(paginaAtual, tamanhoPagina, lista);
	}
	
	@GetMapping("/listagemCompleta")
	public Map<String,Object> listagemCompleta(@RequestHeader Map<String,String> header, 
												@RequestParam(required = false) Integer usuarioCriadorId,
												@RequestParam(required = false) Integer usuarioAtribuidoId,
												@RequestParam(required = false) Integer generoJornalisticoId, 
												@RequestParam(required = false) String titulo,
												@RequestParam(required = false) String editora,
												@RequestParam(required = false) String classificacao,
												@RequestParam(required = false) String subclassificacao,
												@RequestParam(required = false) String feedback,
												@RequestParam(required = false) String noticiaPortalLocal,
												@RequestParam(required = false) String status,
												@RequestParam(required = false) String isVinculado,
												@RequestParam(required = false) String dataInicial, 
												@RequestParam(required = false) String dataFinal,
												@RequestParam(required = false) String ordenacao_data,
												@RequestParam(required = false) Integer paginaAtual, 
												@RequestParam(required = false) Integer tamanhoPagina) throws ApiRequestException{
		
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		Feedback feedback_enum = feedbackValido(feedback);
		Status_clipping status_enum = statusValido(status);
		Noticia_portal_local noticiaPortalLocal_enum = noticiaPortalValido(noticiaPortalLocal);
		LocalDate data_inicial = null;
		LocalDate data_final = null;
		
		if(feedback != null && feedback_enum == null) 
			throw new ApiRequestException("Feedback do clipping pode ser positivio,negativo ou neutro", HttpStatus.BAD_REQUEST);
		if(status != null && status_enum == null)
			throw new ApiRequestException("Status do clipping pode ser aberto,fechado ou anulado", HttpStatus.BAD_REQUEST);
		if(noticiaPortalLocal != null && noticiaPortalLocal_enum == null)
			throw new ApiRequestException("Campo noticiaPortalLocal do clipping pode receber sim ou nao", HttpStatus.BAD_REQUEST);
		if(isVinculado != null && !isVinculado.equals("null") && !isVinculado.equals("true") && !isVinculado.equals("false"))
			throw new ApiRequestException("Campo isVinculado pode receber true,false ou null", HttpStatus.BAD_REQUEST);
		if(dataInicial != null) {
			try {
				data_inicial = LocalDate.parse(dataInicial);
			} catch (DateTimeParseException e) {
				throw new ApiRequestException("Data inicial invalida ou em formato inadequado."
						+ "Lembre-se que a data deve está no formato AAAA-MM-DD", HttpStatus.BAD_REQUEST);
			}
		}
		if(dataFinal != null) {
			try {
				data_final = LocalDate.parse(dataFinal);
			} catch (DateTimeParseException e) {
				throw new ApiRequestException("Data final invalida ou em formato inadequado."
						+ "Lembre-se que a data deve está no formato AAAA-MM-DD", HttpStatus.BAD_REQUEST);
			}
		}
		if(ordenacao_data != null && !ordenacao_data.equals("asc") && !ordenacao_data.equals("desc"))
			throw new ApiRequestException("Ordenação por data pode receber asc ou desc", HttpStatus.BAD_REQUEST);	
			
		return customClippingDAO.listagemCustomizada(usuarioCriadorId, usuarioAtribuidoId, generoJornalisticoId, titulo, editora, classificacao, 
													 subclassificacao, feedback_enum, noticiaPortalLocal_enum, status_enum, isVinculado, 
													 data_inicial, data_final, ordenacao_data, paginaAtual, tamanhoPagina);
	}
	
	@GetMapping("/{id}/estaVinculadoChamado")
	public Map<String,Object> estaVinculadoChamado(@PathVariable Integer id, @RequestHeader Map<String, String> header) throws ApiRequestException {
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		Optional<Clipping> aux = clippingDAO.findById(id);
		if(!aux.isPresent())
			throw new ApiRequestException("Não existe clipping com id = "+id, HttpStatus.BAD_REQUEST);
		
		Clipping clipping = aux.get();
		boolean isVinculado = false;
		Integer chamadoId = null;
		Chamado chamado = clipping.getChamado();
		if(chamado != null) {
			isVinculado = true;
			chamadoId = chamado.getId();
		}
		Map<String,Object> result = new HashMap<String,Object>();
		result.put("isVinculado", isVinculado);
		result.put("id",chamadoId);
		return result;
	}
	
	private void saveFile(String uploadDir, String fileName,
            MultipartFile multipartFile) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
         
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
         
        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {        
            throw new IOException("Could not save image file: " + fileName, ioe);
        }      
    }
	
	private Status_clipping statusValido(String texto) {
		for(Status_clipping t : Status_clipping.values()) {
			if(t.name().equals(texto)) {
				return Status_clipping.valueOf(texto);
			}
		}
		return null;
	}
	
	private Feedback feedbackValido(String texto) {
		for(Feedback t : Feedback.values()) {
			if(t.name().equals(texto)) {
				return Feedback.valueOf(texto);
			}
		}
		return null;
	}
	
	private Noticia_portal_local noticiaPortalValido(String texto) {
		for(Noticia_portal_local t : Noticia_portal_local.values()) {
			if(t.name().equals(texto)) {
				return Noticia_portal_local.valueOf(texto);
			}
		}
		return null;
	}
}
