package com.ES2.ASCOM.repository;

import java.time.LocalDate;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import com.ES2.ASCOM.enums.Status_chamado;
import com.ES2.ASCOM.enums.Tipo;
import com.ES2.ASCOM.exception.ApiRequestException;
import com.ES2.ASCOM.model.Chamado;
import com.ES2.ASCOM.pagination.Paginacao;

@Repository
public class CustomChamadoDAO {
	private final EntityManager em;
	private final Paginacao<Chamado> pag;

    public CustomChamadoDAO(EntityManager em) {
        this.em = em;
        this.pag = new Paginacao<Chamado>();
    }
    
    public Map<String,Object> listagemCustomizada(Integer usuarioCriadorId, Integer usuarioAtribuidoId, 
    											  String titulo, String setor, String ramal,
    											  Tipo tipo, Status_chamado status, 
    											  LocalDate data_inicial, LocalDate data_final,
    											  String ordenacao_id, Integer paginaAtual, 
    											  Integer tamanhoPagina ) throws ApiRequestException{
    	
    	if(ordenacao_id == null) ordenacao_id = "desc";
    	String query = "select chamado from Chamado as chamado ";
    	
    	QueryBuilder<Chamado> queryBuilder = new QueryBuilder<Chamado>();
    	queryBuilder.setSelect(query);
    	queryBuilder.addConditionLike("chamado.nome", "titulo", titulo);
    	queryBuilder.addConditionLike("chamado.setor", "setor", setor);
    	queryBuilder.addConditionLike("chamado.ramal", "ramal", ramal);
    	queryBuilder.addConditionEquals("chamado.tipo","tipo", tipo);
    	queryBuilder.addConditionEquals("chamado.status","status", status);
    	queryBuilder.addConditionEquals("chamado.usuario.id","usuarioCriadorId", usuarioCriadorId);
    	queryBuilder.addConditionEquals("chamado.usuario_atribuido.id","usuarioAtribuidoId", usuarioAtribuidoId);
    	
    	if(data_inicial != null || data_final != null) {
    		if (data_inicial == null) data_inicial = LocalDate.parse("2021-06-01");
    		if (data_final == null) data_final = LocalDate.now();
    		queryBuilder.addConditionBetween("chamado.data_publicacao", "data_inicial", data_inicial, "data_final", data_final);
    	}
    	queryBuilder.addConditionOrderBy("chamado.id", ordenacao_id);
    	
    	
    	TypedQuery<Chamado> tQuery = queryBuilder.buildQuery(em, Chamado.class);
    	//tQuery.getResultList().forEach(usuario -> usuario.setSenha(null));
    	
    	return pag.paginarLista(paginaAtual, tamanhoPagina, tQuery.getResultList());
    }
}
