package com.ES2.ASCOM.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "imagem_contato", schema = "ascom")
public class ImagemContato {

	@Id
	@Column(name = "imagem_contato_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@JsonIgnore
	@OneToOne()
    @JoinColumn(name = "contato_id", referencedColumnName = "contato_id", nullable=false)
    private Contato contato;
	
	@JsonIgnore
	@Column(name = "caminho_absoluto")
	private String caminho_absoluto;
	
	@Column(name = "nome")
	private String nome;
	
	@Column(name = "tamanho")
	private Integer tamanhoBytes;

}

