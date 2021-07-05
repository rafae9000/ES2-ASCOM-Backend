package com.ES2.ASCOM.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ES2.ASCOM.model.GeneroJornalistico;

@Repository
public interface GeneroJornalisticoDAO  extends JpaRepository<GeneroJornalistico, Integer> {

}
