package com.ES2.ASCOM.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ES2.ASCOM.model.Permissao;
import com.ES2.ASCOM.model.Usuario;

@Repository
public interface UsuarioDAO extends JpaRepository<Usuario, Integer> {
	
	/*@Query("SELECT user from Usuario user where user.email = :email")
	public Optional<Usuario> findByEmail(@Param("email") String email);*/
	
	@Query("SELECT user from Usuario user where user.email = :email")
	Optional<Usuario> findByEmail(String email);
	
	
	
	List<Usuario> findByNome(String nome);
	
}
