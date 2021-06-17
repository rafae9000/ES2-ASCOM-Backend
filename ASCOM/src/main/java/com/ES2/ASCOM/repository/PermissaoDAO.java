package com.ES2.ASCOM.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import com.ES2.ASCOM.model.Permissao;

@Repository
public interface PermissaoDAO extends JpaRepository<Permissao, Integer> {

}
