package com.ES2.ASCOM.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.ES2.ASCOM.helpers.Authentication;
import com.ES2.ASCOM.model.Clipping;
import com.ES2.ASCOM.model.GeneroJornalistico;
import com.ES2.ASCOM.model.Usuario;
import com.ES2.ASCOM.pagination.Paginacao;
import com.ES2.ASCOM.repository.ClippingDAO;
import com.ES2.ASCOM.repository.GeneroJornalisticoDAO;

@RestController
@RequestMapping("/generoJornalistico")
public class GeneroJornalisticoController {
	
	
	@Autowired
	private GeneroJornalisticoDAO generoJornalisticoDAO;
	@Autowired
	private ClippingDAO clippingDAO;
	@Autowired
	private Authentication authentication;
	
	private final Paginacao<GeneroJornalistico> pag = new Paginacao<GeneroJornalistico>();
	
	@GetMapping("/generoJaExiste")
	public Map<String,Boolean> generoJaExiste(@RequestHeader Map<String,String> header,@RequestParam String nomeGenero) throws ApiRequestException{
		
		Usuario logged_user = authentication.authenticateUser(header.get("token"));
		Boolean exists;
		
		Optional<GeneroJornalistico> aux = generoJornalisticoDAO.findByNome(nomeGenero);
		if(aux.isPresent()) {
			exists = true;
		}
		else {
			exists = false;
		}
		Map<String,Boolean> result = new HashMap<String,Boolean>();
		result.put("exists", exists);
		return result;
	}
	
	@GetMapping("/generoEstaSendoUsado")
	public Map<String,Boolean> generoEstaSendoUsado(@RequestHeader Map<String,String> header, @RequestParam String nomeGenero) throws ApiRequestException{
		
		Usuario logged_user = authentication.authenticateUser(header.get("token"));
		Boolean isUsed;
		
		Optional<GeneroJornalistico> aux = generoJornalisticoDAO.findByNome(nomeGenero);
		if(!aux.isPresent()) 
			throw new ApiRequestException("Não existe gênero jornalistico com este nome", HttpStatus.BAD_REQUEST);
		
		GeneroJornalistico genero = aux.get();	
		List<Clipping> lista = clippingDAO.findByGeneroJornalistico(genero.getId());
		if(lista.size() == 0) {
			isUsed = false;
		}
		else {
			isUsed = true;
		}
		
		Map<String,Boolean> result = new HashMap<String,Boolean>();
		result.put("isUsed", isUsed);
		return result;
	}
	
	@PostMapping("/salvar")
	public Map<String,Integer> addGenero(@RequestHeader Map<String,String> header, @RequestBody Map<String,String> json) throws ApiRequestException{
		
		Usuario logged_user = authentication.authenticateUser(header.get("token"));
		
		if(!logged_user.getGrupo().getNome().equals("administrador"))
			throw new ApiRequestException("Somente um administrador pode criar um novo Gênero Jornalistico", HttpStatus.FORBIDDEN);
		
		String nomeGenero = json.get("nomeGenero");
		this.isNomeValido(nomeGenero);
		
		Map<String,Boolean> map = this.generoJaExiste(header, nomeGenero);
		Boolean exists = map.get("exists");
		if(exists)
			throw new ApiRequestException("Gênero jornalistico com este nome ja existe", HttpStatus.BAD_REQUEST);
		
		GeneroJornalistico genero = new GeneroJornalistico(null, nomeGenero, null);
		genero = generoJornalisticoDAO.save(genero);
		
		Map<String,Integer> result = new HashMap<String,Integer>();
		result.put("id", genero.getId());
		return result;
	}
	
	@PutMapping("/{id}/alterarNome")
	public Map<String,String> alterarNome(@PathVariable Integer id, @RequestHeader Map<String,String> header, @RequestBody Map<String,String> json) throws ApiRequestException{
		
		Usuario logged_user = authentication.authenticateUser(header.get("token"));
		
		if(!logged_user.getGrupo().getNome().equals("administrador"))
			throw new ApiRequestException("Somente um administrador pode alterar o nome do Gênero Jornalistico", HttpStatus.FORBIDDEN);
		
		String nomeGenero = json.get("nomeGenero");
		this.isNomeValido(nomeGenero);
		
		Map<String,Boolean> map = this.generoJaExiste(header, nomeGenero);
		Boolean exists = map.get("exists");
		if(exists)
			throw new ApiRequestException("Gênero jornalistico com este nome ja existe", HttpStatus.BAD_REQUEST);
		
		Optional<GeneroJornalistico> generoAux = generoJornalisticoDAO.findById(id);
		if(!generoAux.isPresent())
			throw new ApiRequestException("Gênero jornalistico com id = "+id+" não existe", HttpStatus.BAD_REQUEST);
		
		GeneroJornalistico genero = generoAux.get();
		genero.setNome(nomeGenero);
		generoJornalisticoDAO.save(genero);
		
		Map<String,String> result = new HashMap<String,String>();
		result.put("message","Nome do genero foi alterado com sucesso");
		return result;
	}
	
	@DeleteMapping("/{id}/deletar")
	public Map<String,String> deletar(@PathVariable Integer id, @RequestHeader Map<String,String> header) throws ApiRequestException{
		
		Usuario logged_user = authentication.authenticateUser(header.get("token"));
		
		if(!logged_user.getGrupo().getNome().equals("administrador"))
			throw new ApiRequestException("Somente um administrador pode alterar o nome do Gênero Jornalistico", HttpStatus.FORBIDDEN);
		
		Optional<GeneroJornalistico> generoAux = generoJornalisticoDAO.findById(id);
		if(!generoAux.isPresent())
			throw new ApiRequestException("Gênero jornalistico com id = "+id+" não existe", HttpStatus.BAD_REQUEST);
		
		GeneroJornalistico genero = generoAux.get();
		
		Map<String,Boolean> map2 = this.generoEstaSendoUsado(header, genero.getNome());
		Boolean isUsed = map2.get("isUsed");
		if(isUsed)
			throw new ApiRequestException("Esse gênero jornalistico ja esta sendo utilizado em um clipping,portanto não pode ser excluido", 
					                       HttpStatus.BAD_REQUEST);
		
		generoJornalisticoDAO.delete(genero);
		
		Map<String,String> result = new HashMap<String,String>();
		result.put("message","Gênero foi excluido com sucesso");
		return result;
	}
	
	@GetMapping("/listar")
	public Map<String,Object> listar(@RequestHeader Map<String,String> header, 
									 @RequestParam(required = false) Integer paginaAtual, 
									 @RequestParam(required = false) Integer tamanhoPagina) throws ApiRequestException{
		
		Usuario logged_user = authentication.authenticateUser(header.get("token"));
		
		List<GeneroJornalistico> lista = generoJornalisticoDAO.findAll();
		
		return pag.paginarLista(paginaAtual, tamanhoPagina, lista);
	}
	
	private void isNomeValido(String nomeGenero) throws ApiRequestException {
		if(nomeGenero == null) throw new ApiRequestException("Informe o novo nome do gênero jornalistico", HttpStatus.BAD_REQUEST);
		if(nomeGenero.length() == 0 || nomeGenero.trim().length() == 0) throw new ApiRequestException("Nome invalido", HttpStatus.BAD_REQUEST);
		if(nomeGenero.length() > 50) throw new ApiRequestException("Tamano maximo do nome e de 50 caracteres", HttpStatus.BAD_REQUEST);
	}

}
