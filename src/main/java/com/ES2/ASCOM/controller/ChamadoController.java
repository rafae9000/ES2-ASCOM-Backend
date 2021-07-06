package com.ES2.ASCOM.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ES2.ASCOM.exception.ApiRequestException;
import com.ES2.ASCOM.helpers.TokenService;
import com.ES2.ASCOM.repository.ChamadoDAO;
import com.ES2.ASCOM.repository.ClippingDAO;
import com.ES2.ASCOM.repository.CustomChamadoDAO;
import com.ES2.ASCOM.repository.UsuarioDAO;
import com.ES2.ASCOM.enums.Tipo;
import com.ES2.ASCOM.enums.Status_chamado;
import com.ES2.ASCOM.model.Chamado;
import com.ES2.ASCOM.model.Clipping;
import com.ES2.ASCOM.model.Grupo;
import com.ES2.ASCOM.model.Usuario;
import com.ES2.ASCOM.pagination.Paginacao;


@RestController
@RequestMapping("/chamado")
public class ChamadoController {
	
	//!email.endsWith("@ebserh.gov.br")
	
	private static final String EMAIL_PATTERN = 
	        "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
	        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	
	private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);
	
	@Autowired
	private ChamadoDAO chamadoDAO;
	@Autowired
	private UsuarioDAO usuarioDAO;
	@Autowired
	private ClippingDAO clippingDAO;
	@Autowired
	private CustomChamadoDAO customChamadoDAO;
	
	private TokenService tokenService = new TokenService();
	private final Paginacao<Chamado> pag = new Paginacao<Chamado>();
	
	private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private Validator validator = factory.getValidator();
	
	@PostMapping("/salvarChamadoExterno")
	public Map<String,Object> salvarChamadoExterno(@RequestBody Map<String, String> json) throws ApiRequestException{
		String titulo = json.get("titulo");
		String nome = json.get("nome");
		String setor = json.get("setor");
		String telefone = json.get("telefone");
		String email = json.get("email");
		String ramal = json.get("ramal");
		String detalhes_solicitacao = json.get("detalhes_solicitacao");
		String centro_custo_requisicao = json.get("centro_custo_requisicao");
		String localizacao = json.get("localizacao");
		String tipo = json.get("tipo");
		
		
		Tipo tipo_enum =  tipoValido(tipo);
		if(tipo_enum == null) throw new ApiRequestException("Tipo deve receber requisicao ou incidente", HttpStatus.BAD_REQUEST);
		
		Status_chamado status = Status_chamado.aberto;
		LocalDate data = LocalDate.now();
		Chamado chamado = new Chamado(null, null, null, null, titulo, nome, setor, telefone, email, ramal, 
		          					  detalhes_solicitacao, centro_custo_requisicao, localizacao, status, 
		          					  tipo_enum, data, null);
		
	
		Set<ConstraintViolation<Chamado>> violations = validator.validate(chamado);
		for (ConstraintViolation<Chamado> violation : violations) {
			throw new ApiRequestException(violation.getMessage(), HttpStatus.BAD_REQUEST);
		}
		
		chamado = chamadoDAO.save(chamado);
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("message", "Chamado externo criado com sucesso");
		result.put("id",chamado.getId());
		return result;
	}

	@PostMapping("/salvarChamadoInterno")
	public Map<String,Object> salvarChamadoInterno(@RequestHeader Map<String, String> header, @RequestBody Map<String, String> json) 
			throws ApiRequestException{
		
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		String titulo = json.get("titulo");
		String setor = json.get("setor");
		String telefone = json.get("telefone");
		String ramal = json.get("ramal");
		String detalhes_solicitacao = json.get("detalhes_solicitacao");
		String centro_custo_requisicao = json.get("centro_custo_requisicao");
		String localizacao = json.get("localizacao");
		String tipo = json.get("tipo");
		
		String email = logged_user.getEmail();
		String nome = logged_user.getNome();
		
		Integer usuario_atribuido_id = null;
		Usuario usuario_atribuido = null;
		Tipo tipo_enum =  tipoValido(tipo);
		if(tipo_enum == null) throw new ApiRequestException("Tipo deve receber requisicao ou incidente", HttpStatus.BAD_REQUEST);
		
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

		Status_chamado status = Status_chamado.aberto;
		LocalDate data = LocalDate.now();
		Chamado chamado = new Chamado(null, logged_user, usuario_atribuido, null, titulo, nome, setor, telefone, email, ramal, 
		          					  detalhes_solicitacao, centro_custo_requisicao, localizacao, status, tipo_enum, data, null);
		
		Set<ConstraintViolation<Chamado>> violations = validator.validate(chamado);
		for (ConstraintViolation<Chamado> violation : violations) {
			throw new ApiRequestException(violation.getMessage(), HttpStatus.BAD_REQUEST);
		}
		chamado = chamadoDAO.save(chamado);
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("message", "Chamado interno criado com sucesso");
		result.put("id",chamado.getId());
		return result;
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
		
		Optional<Chamado> aux = chamadoDAO.findById(id);
		if(!aux.isPresent())
			throw new ApiRequestException("Não existe um chamado com id = "+id, HttpStatus.BAD_REQUEST);
		
		Chamado chamado = aux.get();
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
		chamado.setUsuario_atribuido(usuario_atribuido);
		chamadoDAO.save(chamado);
		
		Map<String, String> result = new HashMap<String, String>();
		result.put("message", "Chamado foi atribuido com sucesso");
		
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
		
		Optional<Chamado> aux = chamadoDAO.findById(id);
		if(!aux.isPresent())
			throw new ApiRequestException("Não existe um chamado com id = "+id, HttpStatus.BAD_REQUEST);
		
		Chamado chamado = aux.get();
		
		if(!grupo.getNome().equals("administrador") && !grupo.getNome().equals("secretario"))
			throw new ApiRequestException("Apenas um administrador ou secretario pode avaliar um chamado", HttpStatus.FORBIDDEN);
		if(!chamado.getStatus().toString().equals("aberto") && !grupo.getNome().equals("administrador"))
			throw new ApiRequestException("Apenas um administrador pode avaliar um chamado que não está aberto", HttpStatus.FORBIDDEN);
		
		
		String titulo = json.get("titulo");
		String nome = json.get("nome");
		String setor = json.get("setor");
		String telefone = json.get("telefone");
		String email = json.get("email");
		String ramal = json.get("ramal");
		String detalhes_solicitacao = json.get("detalhes_solicitacao");
		String centro_custo_requisicao = json.get("centro_custo_requisicao");
		String localizacao = json.get("localizacao");
		String tipo = json.get("tipo");
		String status = json.get("status");
		String justificativa = json.get("justificativa");
		
		Integer usuario_atribuido_id = null;
		Usuario usuario_atribuido = null;
		Tipo tipo_enum =  tipoValido(tipo);
		Status_chamado status_enum = statusValido(status);
		
		try {		
			String ajudante = json.get("usuario_atribuido_id");
			if(ajudante != null) {
				usuario_atribuido_id = Integer.valueOf(json.get("usuario_atribuido_id"));
				Optional<Usuario> user = usuarioDAO.findById(usuario_atribuido_id );
				if(!user.isPresent())
					 throw new ApiRequestException("Não existe um usuario com o id = "+usuario_atribuido_id, HttpStatus.BAD_REQUEST);
				
				usuario_atribuido = user.get();	
			}
		} catch (NumberFormatException e) {
			throw new ApiRequestException("Id do usuario atribuido deve ser um numero inteiro positivo ou null",HttpStatus.BAD_REQUEST);
		}

		if(tipo_enum == null) throw new ApiRequestException("Tipo deve receber requisicao ou incidente", HttpStatus.BAD_REQUEST);
		if(status_enum == null) throw new ApiRequestException("Status deve receber aberto, solucionado ou anulado", HttpStatus.BAD_REQUEST);
		if(justificativa == null) throw new ApiRequestException("Justificativa não informada", HttpStatus.BAD_REQUEST);
		if(justificativa.trim().isEmpty()) throw new ApiRequestException("Justificativa inválida", HttpStatus.BAD_REQUEST);
		
		//caso o chamado não for externo ou o usuario for um administrador, a edição dos dados podem ser feitas
		if(grupo.getNome().equals("administrador") || chamado.getUsuario() != null) {
			chamado.setTitulo(titulo);
			chamado.setNome(nome);
			chamado.setSetor(setor);
			chamado.setTelefone(telefone);
			chamado.setEmail(email);
			chamado.setRamal(ramal);
			chamado.setDetalhes_solicitacao(detalhes_solicitacao);
			chamado.setCentro_custo_requisicao(centro_custo_requisicao);
			chamado.setLocalizacao(localizacao);
			chamado.setTipo(tipo_enum);
		}
		chamado.setStatus(status_enum);
		chamado.setJustificativa(justificativa);
		chamado.setUsuario_atribuido(usuario_atribuido);
		
		Set<ConstraintViolation<Chamado>> violations = validator.validate(chamado);
		for (ConstraintViolation<Chamado> violation : violations) {
			throw new ApiRequestException(violation.getMessage(), HttpStatus.BAD_REQUEST);
		}
		
		chamado = chamadoDAO.save(chamado);
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("message", "Chamado avaliado com sucesso");
		return result;
	}

	@PutMapping("/{id}/alterar")
	public Map<String,Object> alterar(@PathVariable Integer id, @RequestHeader Map<String, String> header, @RequestBody Map<String, String> json) throws ApiRequestException {
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		Optional<Chamado> aux = chamadoDAO.findById(id);
		if (!aux.isPresent()) 
			throw new ApiRequestException("Chamado com id = "+id+" não existe", HttpStatus.BAD_REQUEST);
		
		Chamado chamado = aux.get();
		Grupo grupo = logged_user.getGrupo();
		Integer criador_chamado_id = chamado.getUsuario().getId();
		if(!grupo.getNome().equals("administrador")) {
			if(chamado.getUsuario() == null)
				throw new ApiRequestException("Somente um administrador pode alterar um chamado externo", HttpStatus.FORBIDDEN);
			else if(!chamado.getStatus().toString().equals("aberto")) 
				throw new ApiRequestException("Somente um administrador pode alterar um chamados que não estão em aberto", HttpStatus.FORBIDDEN);
			if(logged_user_id != criador_chamado_id)
				throw new ApiRequestException("Somente o administrador ou o criador deste chamado pode alterar os dados", HttpStatus.FORBIDDEN);
			
		}
		
		String titulo = json.get("titulo");
		String nome = json.get("nome");
		String setor = json.get("setor");
		String telefone = json.get("telefone");
		String email = json.get("email");
		String ramal = json.get("ramal");
		String detalhes_solicitacao = json.get("detalhes_solicitacao");
		String centro_custo_requisicao = json.get("centro_custo_requisicao");
		String localizacao = json.get("localizacao");
		String tipo = json.get("tipo");
		String justificativa = json.get("justificativa");
		
		Integer usuario_atribuido_id = null;
		Usuario usuario_atribuido = null;
		Tipo tipo_enum =  tipoValido(tipo);
		
		try {		
			String ajudante = json.get("usuario_atribuido_id");
			if(ajudante != null)
				usuario_atribuido_id = Integer.valueOf(json.get("usuario_atribuido_id"));
		} catch (NumberFormatException e) {
			throw new ApiRequestException("Id do usuario atribuido deve ser um numero inteiro positivo ou null",HttpStatus.BAD_REQUEST);
		}

		if(usuario_atribuido_id != null) {
			Optional<Usuario> user = usuarioDAO.findById(usuario_atribuido_id );
			if(!user.isPresent())
				 throw new ApiRequestException("Não existe um usuario com o id = "+usuario_atribuido_id, HttpStatus.BAD_REQUEST);
			
			usuario_atribuido = user.get();	
		}

		if(tipo_enum == null) throw new ApiRequestException("Tipo deve receber requisicao ou incidente", HttpStatus.BAD_REQUEST);
		if(justificativa == null) throw new ApiRequestException("Justificativa não informada", HttpStatus.BAD_REQUEST);
		if(justificativa.trim().isEmpty()) throw new ApiRequestException("Justificativa inválida", HttpStatus.BAD_REQUEST);
		
		
		chamado.setTitulo(titulo);
		chamado.setNome(nome);
		chamado.setSetor(setor);
		chamado.setTelefone(telefone);
		chamado.setEmail(email);
		chamado.setRamal(ramal);
		chamado.setDetalhes_solicitacao(detalhes_solicitacao);
		chamado.setCentro_custo_requisicao(centro_custo_requisicao);
		chamado.setLocalizacao(localizacao);
		chamado.setTipo(tipo_enum);
		chamado.setJustificativa(justificativa);
		chamado.setUsuario_atribuido(usuario_atribuido);
		
		Set<ConstraintViolation<Chamado>> violations = validator.validate(chamado);
		for (ConstraintViolation<Chamado> violation : violations) {
			throw new ApiRequestException(violation.getMessage(), HttpStatus.BAD_REQUEST);
		}
		
		chamado = chamadoDAO.save(chamado);
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("message", "Chamado alterado com sucesso");
		return result;
		
	}
	
	@GetMapping("/{id}")
	public Optional<Chamado> achar(@PathVariable Integer id, @RequestHeader Map<String,String> header) throws ApiRequestException {
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		Optional<Chamado> chamado = chamadoDAO.findById(id);
		if (!chamado.isPresent()) 
			throw new ApiRequestException("Chamado com id = "+id+" não existe", HttpStatus.BAD_REQUEST);
		
		return chamado;
	}
	
	@GetMapping("/listarChamadosAbertosSemAtribuicao")
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
		
		List<Chamado> lista = chamadoDAO.chamadosAbertosSemAtribuicao();
		return pag.paginarLista(paginaAtual, tamanhoPagina, lista);
	}
	
	@GetMapping("/listarChamadosAbertosAtribuidosParaMim")
	public  Map<String,Object> listarChamadosAbertosParaMim(@RequestHeader Map<String,String> header,
															@RequestParam(required = false) Integer paginaAtual, 
															@RequestParam(required = false) Integer tamanhoPagina) throws ApiRequestException{
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		List<Chamado> lista = chamadoDAO.chamadosAbertoAtribuido(logged_user_id);
		return pag.paginarLista(paginaAtual, tamanhoPagina, lista);
	}
	
	@GetMapping("/listagemCompleta")
	public  Map<String,Object> listagemCompleta(@RequestHeader Map<String,String> header, 
												@RequestParam(required = false) Integer usuarioCriadorId,
												@RequestParam(required = false) Integer usuarioAtribuidoId, 
												@RequestParam(required = false) String titulo,
												@RequestParam(required = false) String setor, 
												@RequestParam(required = false) String ramal,
												@RequestParam(required = false) String tipo, 
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
		
		Tipo tipo_enum = tipoValido(tipo);
		Status_chamado status_enum = statusValido(status);
		LocalDate data_inicial = null;
		LocalDate data_final = null;
		
		if(tipo != null && tipo_enum == null) 
			throw new ApiRequestException("Tipo do chamado podem ser requisicao ou incidente", HttpStatus.BAD_REQUEST);
		if(status != null && status_enum == null)
			throw new ApiRequestException("Status do chamado podem ser aberto,solucionado ou anulado", HttpStatus.BAD_REQUEST);	
		if(isVinculado != null && !isVinculado.equals("true") && !isVinculado.equals("false"))
			throw new ApiRequestException("Campo isVinculado pode receber true ou false", HttpStatus.BAD_REQUEST);
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
			
		return customChamadoDAO.listagemCustomizada(usuarioCriadorId, usuarioAtribuidoId, titulo, setor, 
													ramal, tipo_enum, status_enum, isVinculado, data_inicial, data_final, 
													ordenacao_data,paginaAtual, tamanhoPagina);
	}
	
	@GetMapping("/{id}/estaVinculadoClipping")
	public Map<String,Object> estaVinculadoClipping (@PathVariable Integer id, @RequestHeader Map<String,String> header) throws ApiRequestException {
		String token = header.get("token");
		if (token == null)
			throw new ApiRequestException("Token não foi informado", HttpStatus.BAD_REQUEST);

		Integer logged_user_id = tokenService.getTokenSubject(token);
		Usuario logged_user = usuarioDAO.findById(logged_user_id).get();
		
		if(!logged_user.isAtivo())
			throw new ApiRequestException("Sua conta foi recentemente desativada pelo administrador", HttpStatus.FORBIDDEN);
		
		Optional<Chamado> chamadoAux = chamadoDAO.findById(id);
		if (!chamadoAux.isPresent()) 
			throw new ApiRequestException("Chamado com id = "+id+" não existe", HttpStatus.BAD_REQUEST);
		
		boolean isVinculado = false;
		Integer idClipping = null;
		Chamado chamado = chamadoAux.get();
		Optional<Clipping> clippingAux = clippingDAO.findClippingBindWithChamado(chamado.getId());
		if(clippingAux.isPresent()) {
			isVinculado = true;
			Clipping clipping = clippingAux.get();
			idClipping = clipping.getId();
		}
		
		Map<String,Object> result = new HashMap<String,Object>();
		result.put("isVinculado", isVinculado);
		result.put("id",idClipping);
		return result;
	}
	private Tipo tipoValido(String texto) {
		for(Tipo t : Tipo.values()) {
			if(t.name().equals(texto)) {
				return Tipo.valueOf(texto);
			}
		}
		return null;
	}
	
	private Status_chamado statusValido(String texto) {
		for(Status_chamado s : Status_chamado.values()) {
			if(s.name().equals(texto)) {
				return Status_chamado.valueOf(texto);
			}
		}
		return null;
	}
}
