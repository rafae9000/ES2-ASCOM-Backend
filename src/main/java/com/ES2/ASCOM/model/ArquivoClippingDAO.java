package com.ES2.ASCOM.model;

import com.ES2.ASCOM.model.ArquivoClipping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArquivoClippingDAO extends JpaRepository<ArquivoClipping, Integer> {

}
