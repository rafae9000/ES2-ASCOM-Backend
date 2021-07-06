package com.ES2.ASCOM.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ES2.ASCOM.enums.Status_chamado;
import com.ES2.ASCOM.enums.Tipo;
import com.ES2.ASCOM.exception.ApiRequestException;
import com.ES2.ASCOM.model.Chamado;
import com.ES2.ASCOM.model.Clipping;
import com.ES2.ASCOM.pagination.Paginacao;

@Repository
public class CustomChamadoDAO {
	private final EntityManager em;
	private final Paginacao<Chamado> pag;
	@Autowired
	private ClippingDAO clippingDAO;

    public CustomChamadoDAO(EntityManager em) {
        this.em = em;
        this.pag = new Paginacao<Chamado>();
    }
    
    public Map<String,Object> listagemCustomizada(Integer usuarioCriadorId, Integer usuarioAtribuidoId, 
    											  String titulo, String setor, String ramal,
    											  Tipo tipo, Status_chamado status, String isVinculado, 
    											  LocalDate data_inicial, LocalDate data_final,
    											  String ordenacao_id, Integer paginaAtual, 
    											  Integer tamanhoPagina ) throws ApiRequestException{
    	
    	List<Integer> chamadosId = new ArrayList<Integer>();
    	
    	if(ordenacao_id == null) ordenacao_id = "desc";
    	String query = "select chamado from Chamado as chamado ";
    	
    	if(isVinculado != null) {
    		if(isVinculado.equals("true")) {
    			query = "select clipping.chamado from Clipping as clipping ";
    		}
    		else {
    			List<Clipping> lista = clippingDAO.findBindedClippings();
    			if(lista.size() == 0)
    				return pag.paginarLista(paginaAtual, tamanhoPagina, new ArrayList<Chamado>());
    			
    			for(Clipping c : lista) {
    				chamadosId.add(c.getChamado().getId());
    			}
    		}
    	}
    	
    	QueryBuilder<Chamado> queryBuilder = new QueryBuilder<Chamado>();
    	queryBuilder.setSelect(query);
    	queryBuilder.addConditionLike("chamado.titulo", "titulo", titulo);
    	queryBuilder.addConditionLike("chamado.setor", "setor", setor);
    	queryBuilder.addConditionLike("chamado.ramal", "ramal", ramal);
    	queryBuilder.addConditionEquals("chamado.tipo","tipo", tipo);
    	queryBuilder.addConditionEquals("chamado.status","status", status);
    	
    	if(usuarioAtribuidoId != null && usuarioAtribuidoId == 0) {
    		queryBuilder.addConditionIsNull("chamado.usuario_atribuido.id");
    	}
    	else {
    		queryBuilder.addConditionEquals("chamado.usuario_atribuido.id","usuarioAtribuidoId", usuarioAtribuidoId);
    	}
    	
    	if(usuarioCriadorId != null && usuarioCriadorId == 0) {
    		queryBuilder.addConditionIsNull("chamado.usuario.id");
    	}
    	else {
    		queryBuilder.addConditionEquals("chamado.usuario.id","usuarioCriadorId", usuarioCriadorId);
    	}
    	
    	
    	
    	if(data_inicial != null || data_final != null) {
    		if (data_inicial == null) data_inicial = LocalDate.parse("2021-06-01");
    		if (data_final == null) data_final = LocalDate.now();
    		queryBuilder.addConditionBetween("chamado.data_publicacao", "data_inicial", data_inicial, "data_final", data_final);
    	}
    	
    	if(isVinculado != null) {
    		if(isVinculado.equals("true")) {
    			queryBuilder.addConditionIsNotNull("clipping.chamado.id");
    			queryBuilder.addConditionOrderBy("clipping.chamado.id", ordenacao_id);
    		}
    		else {
    			queryBuilder.addConditionNotIn("chamado.id", "chamadosId", chamadosId);
    			queryBuilder.addConditionOrderBy("chamado.id", ordenacao_id);
    		}
    	}
    	else {
    		queryBuilder.addConditionOrderBy("chamado.id", ordenacao_id);
    	}
    	
    	
    	
    	TypedQuery<Chamado> tQuery = queryBuilder.buildQuery(em, Chamado.class);
    	
    	return pag.paginarLista(paginaAtual, tamanhoPagina, tQuery.getResultList());
    }
}
