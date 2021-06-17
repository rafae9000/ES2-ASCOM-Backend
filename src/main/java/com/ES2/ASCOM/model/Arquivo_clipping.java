/*
package com.ES2.ASCOM.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "arquivo_clipping", schema = "ascom")
public class Arquivo_clipping {
	
	@Id
	@Column(name = "arquivo_clipping_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "clipping_id", nullable = false)
	private Clipping clipping;
	
	@Column(name = "caminho_absoluto")
	private String caminho_absoluto;
	
	@Column(name = "chave")
	private String chave;
	
	@Column(name = "codificao")
	private String codificacao;
	
	@Column(name = "tamanho")
	private Integer tamanho;
	
	@Column(name = "tipo")
	private String tipo;

}
*/