package com.ES2.ASCOM.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ES2.ASCOM.model.Chamado;


@Repository
public interface ChamadoDAO extends JpaRepository<Chamado, Integer> {
	
	@Query("SELECT chamado FROM Chamado chamado WHERE chamado.status = 'aberto' "
			+ "AND chamado.usuario_atribuido IS NULL ORDER BY chamado.id")
	List<Chamado> chamadosAbertosSemAtribuicao();
	
	@Query("SELECT chamado FROM Chamado chamado WHERE chamado.status = 'aberto' "
			+ "AND chamado.usuario_atribuido.id = :usuarioId ORDER BY chamado.id")
	List<Chamado> chamadosAbertoAtribuido(Integer usuarioId);
}
