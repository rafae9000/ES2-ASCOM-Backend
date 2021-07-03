package com.ES2.ASCOM.repository;

import com.ES2.ASCOM.model.ArquivoClipping;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ArquivoClippingDAO extends JpaRepository<ArquivoClipping, Integer> {
	
	@Query("SELECT arquivo from ArquivoClipping arquivo where arquivo.caminho_absoluto = :caminho")
	Optional<ArquivoClipping> findByCaminhoAbsoluto(String caminho);
}
