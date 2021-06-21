package com.ES2.ASCOM.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Entity
@Table(name = "grupo", schema = "ascom")
public class Grupo {

	@Id
	@Column(name = "grupo_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column
	private String nome;

	@JsonIgnore
	@OneToMany(mappedBy = "grupo")
	private List<Usuario> usuarios;

	/*
	 * @JsonIgnore
	 * 
	 * @OneToMany(mappedBy="grupo") private List<Grupo_permissao> permissoes;
	 */

	@JsonIgnore
	@ManyToMany(mappedBy = "grupos")
	private List<Permissao> permissoes;

}
