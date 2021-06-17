package com.ES2.ASCOM.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
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
	@OneToMany(mappedBy="grupo")
	private List<Usuario> usuarios; 

	@JsonIgnore
	@ManyToMany
	@JoinTable(name = "grupo_permissao", schema = "ascom",
		joinColumns = @JoinColumn(name="grupo_id"), 
		inverseJoinColumns = @JoinColumn(name="permissao_id")
	)
	private List<Permissao> permissoes;
}
