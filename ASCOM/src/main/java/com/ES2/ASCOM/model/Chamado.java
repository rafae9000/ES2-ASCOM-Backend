/*
package com.ES2.ASCOM.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.ES2.ASCOM.enums.Status;
import com.ES2.ASCOM.enums.Tipo;

import lombok.Data;

@Data
@Entity
@Table(name = "chamado", schema = "ascom")
public class Chamado {
	
	@Id
	@Column(name = "chamado_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;
	
	@Column(name = "titulo")
	private String titulo;
	
	@Column(name = "nome")
	private String nome;
	
	@Column(name = "setor")
	private String setor;
	
	@Column(name = "telefone")
	private String telefone;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "ramal")
	private String ramal;
	
	@Column(name = "detalhes_solicitacao")
	private String detalhes_solicitacao;
	
	@Column(name = "centro_custo_requisicao")
	private String centro_custo_requisicao;
	
	@Column(name = "localizacao")
	private String localizacao;
	
	//aberto,fechado ou anulado
	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private Status status;
	
	//requisicao ou incidente
	@Enumerated(EnumType.STRING)
	@Column(name = "tipo")
	private Tipo tipo;
	
	@Column(name = "justificativa", nullable = true)
	private String justificativa;
	
	@ManyToOne
	@JoinColumn(name = "categoria_id", nullable = false)
	private Categoria categoria;
	
	
	
	

}
*/