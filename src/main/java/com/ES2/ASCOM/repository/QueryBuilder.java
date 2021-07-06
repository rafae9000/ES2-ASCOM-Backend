package com.ES2.ASCOM.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.HashMap;

public class QueryBuilder <T> {

	private List<String> condicoes;
	private HashMap<String, Object> values;
	private String select;
	private boolean orderIsUsed;
	
	public QueryBuilder() {
		this.condicoes = new ArrayList<String>();
		this.select = "";
		this.values = new HashMap<String, Object>(); 
		this.orderIsUsed = false;
	}
	
	public void setSelect(String select) {
		this.select = select;
	}
	
	
	public void addConditionEquals(String field, String param, Object value) {
		if(value != null) {
			String condition = String.format("%s = :%s", field, param);
			condicoes.add(condition);
			values.put(param, value);
		}
	}
	public void addConditionLike(String field, String param, Object value) {
		if(value != null) {
			String condition = String.format("lower(%s) like :%s", field, param);
			condicoes.add(condition);
			values.put(param, "%"+value.toString().toLowerCase()+"%");
		}
	}
	//deve ser usado no final da query e apenas 1 vez
	public void addConditionOrderBy(String field, String order) {
		if(order == null) order = "asc";
		String condition = String.format("order by %s %s", field, order);
		condicoes.add(condition);
		orderIsUsed = true;
	}
	
	public void addConditionBetween(String field, String param1, Object value1, String param2, Object value2) {
		if(value1 != null && value2 != null) {
			String condition = String.format("%s between :%s and :%s", field, param1, param2);
			condicoes.add(condition);
			values.put(param1, value1);
			values.put(param2, value2);
		}
	}
	
	public void addConditionIsNull(String field) {
		String condition = String.format("%s is null", field);
		condicoes.add(condition);
	}
	
	public void addConditionIsNotNull(String field) {
		String condition = String.format("%s is not null", field);
		condicoes.add(condition);
	}
	
	public void addConditionNotIn(String field, String param, Object value) {
		if(value != null) {
			String condition = String.format("%s not in :%s", field, param);
			condicoes.add(condition);
			values.put(param, value);
		}
	}
	
	
	
	public String build() {
		String condicaoCompleta = " where ";
		int tam = this.condicoes.size();
		if(tam == 1 && orderIsUsed) condicaoCompleta = " ";
		for(int i = 0; i <  tam - 1; i++) {
			if(i == tam - 2 && orderIsUsed)
				condicaoCompleta += this.condicoes.get(i)+ " ";
			else
				condicaoCompleta += this.condicoes.get(i) + " and ";
		}
		if(tam == 0)
			return this.select;
		
		condicaoCompleta += this.condicoes.get(tam-1);
		condicaoCompleta = this.select + condicaoCompleta;
		orderIsUsed = false;
		System.out.println(condicaoCompleta);
		return condicaoCompleta;
	}
	
	public TypedQuery<T> buildQuery(EntityManager em, Class<T> type) {
		String stringQuery = this.build();
		TypedQuery<T> query = em.createQuery(stringQuery, type);
		for(String key : this.values.keySet()) {
			Object value = this.values.get(key);
			query.setParameter(key, value);
		}
		return query; 
	}
	
}
