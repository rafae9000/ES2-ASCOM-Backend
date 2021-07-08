package com.ES2.ASCOM.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ES2.ASCOM.model.Clipping;

@Repository
public interface ClippingDAO extends JpaRepository<Clipping, Integer> {

	@Query("SELECT clipping from Clipping clipping where clipping.chamado.id = :chamadoId")
	Optional<Clipping> findClippingBindWithChamado(Integer chamadoId);
	
	@Query("SELECT clipping FROM Clipping clipping WHERE clipping.status = 'aberto' "
			+ "AND clipping.usuario_atribuido IS NULL ORDER BY clipping.id")
	List<Clipping> clippingsAbertosSemAtribuicao();
	
	@Query("SELECT clipping FROM Clipping clipping WHERE clipping.status = 'aberto' "
			+ "AND clipping.usuario_atribuido.id = :usuarioId ORDER BY clipping.id")
	List<Clipping> clippingsAbertoAtribuido(Integer usuarioId);
	
	@Query("SELECT clipping FROM Clipping clipping WHERE clipping.chamado IS NOT NULL")
	List<Clipping> findBindedClippings();
	
	@Query("SELECT clipping FROM Clipping clipping WHERE clipping.genero_jornalistico.id = :generoId")
	List<Clipping> findByGeneroJornalistico(Integer generoId);
	
	
	
}
