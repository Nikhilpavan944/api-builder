package com.cg.osce.apibuilder.service;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cg.osce.apibuilder.pojo.Constants;
import com.cg.osce.apibuilder.pojo.Delete;
import com.cg.osce.apibuilder.pojo.Get;
import com.cg.osce.apibuilder.pojo.Parameter;
import com.cg.osce.apibuilder.pojo.Post;
import com.cg.osce.apibuilder.pojo.Put;
import com.cg.osce.apibuilder.pojo.Responses;
import com.cg.osce.apibuilder.pojo.Schema;
import com.cg.osce.apibuilder.pojo._200;
import com.cg.osce.apibuilder.pojo._400;
import com.cg.osce.apibuilder.pojo._404;
import com.cg.osce.apibuilder.pojo._405;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class XmlSchema2yamlImpl implements IxmlSchema2yaml {

	private static final Logger LOGGER = LoggerFactory.getLogger(XmlSchema2yamlImpl.class);

	@Override
	public Post createDefaultPost(Class<? extends Object> entity, JsonNodeFactory factory)
			throws JsonProcessingException {
		String entityName = entity.getSimpleName();
		Post post = new Post();
		List<String> consumes = new ArrayList<>();
		consumes.add(Constants.XMLAPPLICATION);
		consumes.add(Constants.JSONAPPLICATION);
		List<String> produces = new ArrayList<>();
		produces.add(Constants.XMLAPPLICATION);
		produces.add(Constants.JSONAPPLICATION);
		post.setConsumes(consumes);
		post.setProduces(produces);
		post.setSummary(Constants.POSTSUMMARY + entity.getSimpleName());
		post.setDescription(entityName + Constants.POSTDESCRIPTION);
		post.setOperationId("postOperation" + entity.getSimpleName());
		Responses responses = new Responses();
		responses.set400(new _400());
		responses.set200(new _200());
		post.setResponses(responses);
		List<Parameter> parameters = new ArrayList<>();
		Parameter parameter = new Parameter();
		parameter.setDescription(entity.getSimpleName() + Constants.POSTDESCRIPTION);
		parameter.setIn("body");
		parameter.setName(entity.getSimpleName());
		parameter.setRequired(true);
		Schema schema = new Schema();
		schema.set$ref(Constants.SET$REF + entity.getSimpleName());
		parameter.setSchema(schema);
		parameters.add(parameter);
		post.setParameters(parameters);
		return post;
	}

	@Override
	public Get createDefaultGet(Class<? extends Object> entity, JsonNodeFactory factory) {

		String entityName = entity.getSimpleName();
		Get get = new Get();
		get.setSummary(Constants.GETSUMMARY + entityName);
		get.setDescription(entityName + Constants.GETDESCRIPTION);
		get.setOperationId("getOperation" + entityName);
		List<Parameter> parameters = new ArrayList<>();

		Parameter limitParam = new Parameter();
		limitParam.setName("limit");
		limitParam.setIn("query");
		limitParam.setDescription("Maximum number of elements per page");
		limitParam.setRequired(true);
		limitParam.setType("integer");
		parameters.add(limitParam);

		Parameter queryParam = new Parameter();
		queryParam.setName("page");
		queryParam.setIn("query");
		queryParam.setDescription("Page number");
		queryParam.setRequired(true);
		queryParam.setType("integer");
		parameters.add(queryParam);
		// ----Based on the id ------
		for (Field field : entity.getDeclaredFields()) {

			if (field.getName().toLowerCase().contains("id")) {
				Parameter parameter = new Parameter();
				parameter.setName(field.getName());
				parameter.setIn("query");
				parameter.setDescription(field.getName() + " Entity to be Fetched");
				parameter.setRequired(true);
				parameter.setType(getType(field.getType().getSimpleName()));
				parameters.add(parameter);
			}

		}
		get.setParameters(parameters);
		List<String> produces = new ArrayList<>();
		produces.add("application/xml");
		produces.add("application/json");
		get.setProduces(produces);

		Responses responses = new Responses();
		_200 _200 = new _200();
		_200.setDescription("Successful Response");
		ObjectNode schema = factory.objectNode();
		schema.put("type", "object");
		// entity.p
		ObjectNode properties = factory.objectNode();
		ObjectNode page = factory.objectNode();
		page.put("type", "integer");
		page.put("example", 5);
		properties.set("page", page);

		ObjectNode limit = factory.objectNode();
		limit.put("type", "integer");
		limit.put("example", 10);
		properties.set("limit", limit);

		ObjectNode totalPages = factory.objectNode();
		totalPages.put("type", "integer");
		totalPages.put("example", 10);
		properties.set("totalPages", totalPages);

		ObjectNode totalRecords = factory.objectNode();
		totalRecords.put("type", "integer");
		totalRecords.put("example", 100);
		properties.set("totalRecords", totalRecords);

		ObjectNode pojo = factory.objectNode();
		pojo.put("type", "array");
		ObjectNode items = factory.objectNode();
		items.put("$ref", "#/definitions/" + entity.getSimpleName());
		pojo.putPOJO("items", items);
		properties.set("totalRecords", totalRecords);

		properties.putPOJO("schema", pojo);

		schema.set("properties", properties);
		_200.setSchema(schema);

		responses.set_200(_200);
		get.setResponses(responses);

		return get;
	}
	// --------------------End of Get------------------

	// ---------------------Start of Put----------------
	@Override
	public Put createDefaultPut(Class<? extends Object> entity, JsonNodeFactory factory)
			throws JsonProcessingException {
		String entityName = entity.getSimpleName();

		Put put = new Put();

		put.setSummary("Update an existing " + entity.getSimpleName());
		put.setDescription(entityName + "to be Updated");

		put.setOperationId("update" + entity.getSimpleName());
		List<String> consumes = new ArrayList<>();
		consumes.add("application/json");
		consumes.add("application/xml");
		List<String> produces = new ArrayList<>();
		produces.add("application/json");
		produces.add("application/xml");
		put.setConsumes(consumes);
		put.setProduces(produces);
		List<Parameter> parameters = new ArrayList<>();
		Parameter parameter = new Parameter();
		parameter.setIn("body");
		parameter.setName("body");
		parameter.setDescription(entity.getSimpleName() + "object that needs to be added to the store");
		parameter.setRequired(true);
		Schema schema = new Schema();
		schema.set$ref("#/definitions/" + entity.getSimpleName());
		parameter.setSchema(schema);
		parameters.add(parameter);
		put.setParameters(parameters);
		Responses responses = new Responses();
		responses.set400(new _400());
		responses.set_404(new _404());
		responses.set_405(new _405());
		put.setResponses(responses);

		return put;

	}
	// ----------End of Put-------------

	@Override
	public Delete createDefaultDelete(Class<? extends Object> entity, JsonNodeFactory factory)
			throws JsonProcessingException {
		String entityName = entity.getSimpleName();

		Delete delete = new Delete();
		delete.setSummary("Deletes a " + entity.getSimpleName());
		delete.setDescription(entityName + "to be Deleted");

		delete.setOperationId("delete" + entity.getSimpleName());
		List<String> produces = new ArrayList<>();
		produces.add("application/json");
		produces.add("application/xml");
		delete.setProduces(produces);
		List<Parameter> parameters = new ArrayList<Parameter>();

		Parameter limitParam = new Parameter();
		limitParam.setName("api_key");
		limitParam.setIn("header");
		limitParam.setRequired(false);
		limitParam.setType("string");
		parameters.add(limitParam);

		for (Field field : entity.getDeclaredFields()) {

			if (field.getName().toLowerCase().contains("id")) {
				Parameter parameter = new Parameter();
				parameter.setName(field.getName());
				parameter.setIn("query");
				parameter.setDescription(field.getName() + " to be deleted");
				parameter.setRequired(true);
				parameter.setType(getType(field.getType().getSimpleName()));
				// parameter.setFormat((field.getType().getSimpleName()))
				parameters.add(parameter);
			}
		}
		delete.setParameters(parameters);
		Responses responses = new Responses();
		responses.set400(new _400());
		responses.set_404(new _404());
		delete.setResponses(responses);

		return delete;

	}

	String getType(String fieldType) {

		LOGGER.info("Field Type  " + fieldType.toLowerCase().toString());

		switch (fieldType.toLowerCase()) {

		case "string":
			return "string";
		case "biginteger":
			return "integer";
		case "integer":
			return "integer";
		case "int":
			return "integer"; // Yet to Work
		case "list":
			return "string";
		case "boolean":
			return "boolean";
		case "bigdecimal":
			return "integer";
		case "XMLGregorianCalendar":
			return "string";
		case "qname":
			return "string";
		case "role":
			return "integer";
		case "xmlgregoriancalendar":
			return "string"; // YEt to work
		default:
			return "string";

		}

	}

	@SuppressWarnings("deprecation")
	@Override
	public ObjectNode creteDefinitions(JsonNodeFactory factory) {

		Set<Class<? extends Object>> allClasses = getClassDetails();

		Set<Class<? extends Enum>> allEnums = getEnumDetails();

		ObjectNode schema = factory.objectNode();

		for (Class<? extends Object> obj : allClasses) {
			if ("ObjectFactory".equals(obj.getSimpleName()) || "package-info".equals(obj.getSimpleName()))
				continue;
			ObjectNode entity = factory.objectNode();
			entity.put("type", "object");
			// entity.p
			ObjectNode properties = factory.objectNode();
			for (Field field : obj.getDeclaredFields()) {
				// setProperty based on fieldType
				properties.set(field.getName(), setProperty(field, factory));
			}
			// LOGGER.info("props"+properties)
			entity.set("properties", properties);
			schema.set(obj.getSimpleName(), entity);

		}
		for (Class<? extends Enum> enumEntity : allEnums) {
			ObjectNode entity = factory.objectNode();
			entity.put("type", "string");
			ArrayNode arrayNode = factory.arrayNode();
			for (Field field : enumEntity.getDeclaredFields()) {
				// setProperty based on fieldType
				arrayNode.add(field.getName());
			}
			entity.put("enum", arrayNode);
			schema.set(enumEntity.getSimpleName(), entity);

		}
		LOGGER.info(schema.toString());
		return schema;
	}

	private ObjectNode setProperty(Field field, JsonNodeFactory factory) {
		ObjectNode property = factory.objectNode();
		String type = field.getType().getSimpleName();

		switch (type.toLowerCase()) {

		case "string":
			property.put("type", "string");
			property.put("example", "helloworld");
			break;
		case "long":
			property.put("type", "integer");
			property.put("format", "int64");
			property.put("example", 1253465876);
			break;
		case "short":
			property.put("type", "integer");
			property.put("format", "int");
			property.put("example", 234);
			break;
		case "byte":
			property.put("type", "integer");
			property.put("format", "int");
			property.put("example", 127);
			break;
		case "char":
			property.put("type", "string");
			property.put("example", 'A');
			break;
		case "biginteger":
			property.put("type", "integer");
			property.put("format", "int32");
			property.put("example", 6598741);
			break;
		case "integer":
			property.put("type", "integer");
			property.put("example", 6598);
			break;
		case "int":
			property.put("type", "integer");
			property.put("example", 6985);
			break;
		case "double":
			property.put("type", "number");
			property.put("format", "double");
			property.put("example", 12.7643);
			break;
		case "float":
			property.put("type", "number");
			property.put("format", "float");
			property.put("example", 12.85);
			break;
		case "list":
			ObjectNode items = factory.objectNode();
			property.put("type", "array");
			Type type1 = field.getGenericType();
			if (type1 instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) type1;
				for (Type t : pt.getActualTypeArguments()) {
					String arrayType = t.getTypeName().replace("com.cg.osce.apibuilder.entity.", "");
					if (arrayType.contains("java")) {
						System.err.println(arrayType);
						int length = arrayType.split("\\.").length;
						items.put("type", getType(arrayType.split("\\.")[length - 1]));
					} else {
						items.put("$ref", "#/definitions/" + t.getTypeName().replace("com.cg.osce.apibuilder.entity.", ""));
					}
				}
			}
			property.putPOJO("items", items);
			break;
		case "boolean":
			property.put("type", "boolean");
			property.put("example", "true");
			break;
		case "bigdecimal":
			property.put("type", "number");
			property.put("example", 655498.5456);
			break;
		case "xmlgregoriancalendar":
			property.put("type", "string");
			property.put("format", "date");
			property.put("example", 12252019);
			break;
		// YEt to work
		default:

			// LOGGER.info("Default" + field.getType().getSimpleName())
			// LOGGER.info("--" + field.getType())

			property.put("$ref", "#/definitions/" + field.getType().getSimpleName());
			break;

		}
		return property;
	}

	@Override
	public Set<Class<? extends Object>> getClassDetails() {
		Reflections reflections = new Reflections("com.cg.osce.apibuilder.entity", new SubTypesScanner(false));
		return reflections.getSubTypesOf(Object.class);

	}

	public Set<Class<? extends Enum>> getEnumDetails() {
		Reflections reflections = new Reflections("com.cg.osce.apibuilder.entity", new SubTypesScanner(false));
		return reflections.getSubTypesOf(Enum.class);

	}

}
