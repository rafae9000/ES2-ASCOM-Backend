package com.ES2.ASCOM.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ES2.ASCOM.model.GeneroJornalistico;

@Repository
public interface GeneroJornalisticoDAO  extends JpaRepository<GeneroJornalistico, Integer> {
	
	@Query("SELECT genero FROM GeneroJornalistico genero WHERE genero.nome = :nome")
	Optional<GeneroJornalistico> findByNome(String nome);
	
	@Query("SELECT genero FROM GeneroJornalistico genero ORDER BY genero.id")
	List<GeneroJornalistico> findAll();
}
