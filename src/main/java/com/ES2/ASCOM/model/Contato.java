
package com.ES2.ASCOM.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "contato", schema = "ascom")
public class Contato {
	
	@Id
	@Column(name = "contato_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@OneToOne(mappedBy = "contato")
    private ImagemContato imagem;
	
	@Column(name = "nome")
	private String nome;
	
	@Column(name = "empresa")
	private String empresa;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "setor")
	private String setor;
	
	@Column(name = "telefone")
	private String telefone;
}
