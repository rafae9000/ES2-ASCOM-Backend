package com.ES2.ASCOM.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import com.ES2.ASCOM.model.Grupo;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Entity
@Table(name = "permissao", schema = "ascom")
public class Permissao {

	@Id
	@Column(name = "permissao_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column
	private String permissao;

	/*
	 * @JsonIgnore
	 * 
	 * @OneToMany(mappedBy="permissao") private List<Grupo_permissao> grupos;
	 */

	@JsonIgnore
	@ManyToMany
	@JoinTable(name = "grupo_permissao", schema = "ascom", 
			   joinColumns = @JoinColumn(name = "permissao_id"), 
			   inverseJoinColumns = @JoinColumn(name = "grupo_id"))
	private List<Grupo> grupos;

}
