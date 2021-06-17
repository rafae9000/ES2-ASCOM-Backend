package com.ES2.ASCOM.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ES2.ASCOM.model.Grupo;
import com.ES2.ASCOM.model.Permissao;
import com.ES2.ASCOM.model.Usuario;
import com.ES2.ASCOM.repository.UserRepository;
import com.ES2.ASCOM.repository.PermissaoDAO;;

@RestController
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	PasswordEncoder encoder;
	
	@GetMapping("/listar")
	public Page<Usuario> listar(@PageableDefault(value = 10) Pageable pageable) {
		Page<Usuario> lista =  userRepository.findAll(pageable);
		lista.getContent().forEach(usuario -> usuario.setSenha(null));
		//lista.forEach(usuario -> usuario.setSenha(null));
		return lista;
	}
	
	@PostMapping("/salvar")
	public Usuario salvar(@RequestBody Usuario user) {
		this.encoder = new BCryptPasswordEncoder();
		String encodePassword = this.encoder.encode(user.getSenha());
		user.setSenha(encodePassword);
		return userRepository.save(user);
	}
	
	@PostMapping("/login")
	public String login(@RequestBody Map<String,String> json) {
		String email = json.get("email");
		String senha = json.get("senha");
		Optional<Usuario> user = userRepository.findByEmail(email);
		if(!user.isPresent()) 
			return "Não existe usuario com este email";
			
		Integer usuarioId = user.get().getId();
		Usuario usuario = user.get();
		Grupo grupo = usuario.getGrupo();
		List<Permissao> permissoes = grupo.getPermissoes();
		
		String senha_correta = user.get().getSenha();
		if(!senha_correta.equals(senha))
			return "Senha incorreta";
		System.out.println(permissoes);
		System.out.println(permissoes.toString());
		return "Login feito com sucesso ";
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

}
