
package com.ES2.ASCOM.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "arquivo_clipping", schema = "ascom")
public class ArquivoClipping {
	
	@Id
	@Column(name = "arquivo_clipping_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "clipping_id", nullable = false)
	private Clipping clipping;
	
	@Column(name = "caminho_absoluto")
	private String caminho_absoluto;
	
	@Column(name = "nome")
	private String nome;
	
	@Column(name = "tamanho")
	private Integer tamanhoBytes;

}


