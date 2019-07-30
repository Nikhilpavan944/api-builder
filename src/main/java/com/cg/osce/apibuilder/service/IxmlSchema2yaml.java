package com.cg.osce.apibuilder.service;

import java.util.Set;

import com.cg.osce.apibuilder.pojo.Delete;
import com.cg.osce.apibuilder.pojo.Get;
import com.cg.osce.apibuilder.pojo.Post;
import com.cg.osce.apibuilder.pojo.Put;
import com.cg.osce.apibuilder.pojo.SwaggerSchema;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface IxmlSchema2yaml {

	SwaggerSchema printYAML = null;
	
	public Post createDefaultPost(Class<? extends Object> entity,JsonNodeFactory factory) throws JsonProcessingException;
	
	public Get createDefaultGet(Class<? extends Object> entity,JsonNodeFactory factory);
	
	public Put createDefaultPut(Class<? extends Object> entity,JsonNodeFactory factory) throws JsonProcessingException;
	
	public Delete createDefaultDelete(Class<? extends Object> entity,JsonNodeFactory factory) throws JsonProcessingException;
	
	public ObjectNode creteDefinitions(JsonNodeFactory factory);
	
	public Set<Class<? extends Object>> getClassDetails();

}
