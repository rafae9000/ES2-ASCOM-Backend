package com.ES2.ASCOM.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ES2.ASCOM.model.Permissao;

@Repository
public interface PermissaoDAO extends JpaRepository<Permissao, Integer> {

	@Query("SELECT p from Permissao p inner join p.grupos grupo where grupo.id = ?1")
	List<Permissao> permissoesGrupo(Integer gurpoId);

}
