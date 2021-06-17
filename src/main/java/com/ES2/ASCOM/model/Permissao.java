package com.ES2.ASCOM.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import com.ES2.ASCOM.model.Grupo;

import lombok.Data;


@Data
@Entity
@Table(name = "grupo", schema = "ascom")
public class Permissao {

	@Id
	@Column(name = "permissao_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column
	private String permissao;
	
	@ManyToMany(mappedBy="permissoes")
	private List<Grupo> grupos;

}
