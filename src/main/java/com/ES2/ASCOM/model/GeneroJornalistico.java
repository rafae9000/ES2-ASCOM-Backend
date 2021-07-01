

package com.ES2.ASCOM.model;


import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "genero_jornalistico", schema = "ascom")
public class GeneroJornalistico {
	
	@Id
	@Column(name = "genero_jornalistico_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "nome", unique = true)
	private String nome;
	
	@JsonIgnore
	@OneToMany(mappedBy = "genero_jornalistico")
	private List<Clipping> clippings;
}

