/*

package com.ES2.ASCOM.model;


import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;


import lombok.Data;

@Data
@Entity
@Table(name = "genero_jornalistico", schema = "ascom")
public class Genero_jornalistico {
	
	@Id
	@Column(name = "genero_jornalistico_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "nome", unique = true)
	private String nome;
	
	@OneToMany(mappedBy = "genero_jornalistico")
	private List<Clipping> clippings;
}
*/
