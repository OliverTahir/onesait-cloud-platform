/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.config.services.ontologydata;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.processors.syntax.SyntaxValidator;
import com.minsait.onesait.platform.commons.security.BasicEncryption;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyRelation;
import com.minsait.onesait.platform.persistence.ContextData;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OntologyDataServiceImpl implements OntologyDataService {

	public enum EncryptionOperations {
		ENCRYPT, DECRYPT
	}

	@Autowired
	private OntologyRepository ontologyRepository;

	final private ObjectMapper objectMapper = new ObjectMapper();

	final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

	final public static String ENCRYPT_PROPERTY = "encrypted";

	// TODO this is a basic functionality.
	// TODO it has to be improved. For instance, initVector should be random.
	// TODO review AES best practices to improve this class.
	final static String KEY = "Bar12345Bar12345"; // 128 bit key
	final static String INIT_VECTOR = "RandomInitVector"; // 16 bytes IV
	private final static String PROP_STR = "properties";
	private final static String JSON_ERROR = "Error working with JSON data";
	private final static String REQ_STR = "required";
	private final static String REQ_PROP_STR = "Required properties of new schema do not match old schema";

	final private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
			.withZone(ZoneId.of("UTC"));

	@Override
	public void checkOntologySchemaCompliance(final JsonNode data, final Ontology ontology)
			throws DataSchemaValidationException {
		try {
			final JsonNode jsonSchema = ontologyRepository.getSchemaAsJsonNode(ontology);
			// final JsonNode jsonSchema = objectMapper.readTree(ontology.getJsonSchema());
			checkJsonCompliantWithSchema(data, jsonSchema);
		} catch (final IOException e) {
			throw new DataSchemaValidationException("Error reading data for checking schema compliance", e);
		} catch (final ProcessingException e) {
			throw new DataSchemaValidationException("Error checking data schema compliance", e);
		}
	}

	void checkJsonCompliantWithSchema(final JsonNode data, final JsonNode schemaJson)
			throws ProcessingException, DataSchemaValidationException {
		final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
		final JsonSchema schema = factory.getJsonSchema(schemaJson);
		try {
			final ProcessingReport report = schema.validate(data);
			if (report != null && !report.isSuccess()) {
				final Iterator<ProcessingMessage> it = report.iterator();
				final StringBuffer msgerror = new StringBuffer();
				while (it.hasNext()) {
					final ProcessingMessage msg = it.next();
					if (msg.getLogLevel().equals(LogLevel.ERROR)) {
						msgerror.append(msg.asJson());
					}
				}

				throw new DataSchemaValidationException(
						"Error processing data:" + data.toString() + "by:" + msgerror.toString());
			}

		} catch (final DataSchemaValidationException e) {
			throw e;
		} catch (final Exception e) {
			log.error("", e);
			throw new DataSchemaValidationException(
					"Error processing data:" + data.toString() + "by:" + e.getMessage());
		}
	}

	void checkJsonCompliantWithSchema(final String dataString, final String schemaString)
			throws DataSchemaValidationException {
		JsonNode dataJson;
		JsonNode schemaJson;

		try {
			dataJson = JsonLoader.fromString(dataString);
			schemaJson = JsonLoader.fromString(schemaString);
			checkJsonCompliantWithSchema(dataJson, schemaJson);

		} catch (final IOException e) {
			throw new DataSchemaValidationException("Error reading data for checking schema compliance", e);
		} catch (final ProcessingException e) {
			throw new DataSchemaValidationException("Error checking data schema compliance", e);
		}
	}

	String addContextData(final OperationModel operationModel, JsonNode data)
			throws JsonProcessingException, IOException {

		final String body = operationModel.getBody();
		final String user = operationModel.getUser();
		final String clientConnection = operationModel.getClientConnection();
		final String deviceTemplate = operationModel.getDeviceTemplate();
		final String device = operationModel.getDevice();
		final String clientSession = operationModel.getClientSession();
		final String source = operationModel.getSource().name();

		final String timezoneId = ZoneId.of("UTC").toString();
		final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
		final String timestamp = now.format(formatter);

		final long timestampMillis = System.currentTimeMillis();
		final ContextData contextData = ContextData.builder(user, timezoneId, timestamp, timestampMillis, source)
				.clientConnection(clientConnection).deviceTemplate(deviceTemplate).device(device)
				.clientSession(clientSession).build();

		final JsonNode jsonBody;
		if (data == null)
			jsonBody = objectMapper.readTree(body);
		else
			jsonBody = data;
		if (jsonBody.isObject()) {
			final ObjectNode nodeBody = (ObjectNode) jsonBody;
			nodeBody.set("contextData", objectMapper.valueToTree(contextData));
			return objectMapper.writeValueAsString(nodeBody);
		} else {
			throw new IllegalStateException("Body should have a valid json object");
		}

	}

	String encryptionOperation(String data, Ontology ontology, EncryptionOperations operation) throws IOException {

		if (ontology.isAllowsCypherFields()) {

			final JsonNode jsonSchema = objectMapper.readTree(ontology.getJsonSchema());
			final JsonNode jsonData = objectMapper.readTree(data);
			final String path = "#";
			final String schemaPointer = "";

			processProperties(jsonData, jsonSchema, jsonSchema, path, schemaPointer, operation);

			return jsonData.toString();

		} else {
			return data;
		}

	}

	private void processProperties(JsonNode allData, JsonNode schema, JsonNode rootSchema, String path,
			String schemaPointer, EncryptionOperations operation) {

		final JsonNode properties = schema.path(PROP_STR);
		final Iterator<Entry<String, JsonNode>> elements = properties.fields();

		while (elements.hasNext()) {
			final Entry<String, JsonNode> element = elements.next();
			if (element != null) {
				processProperty(allData, element.getKey(), element.getValue(), rootSchema,
						path + "/" + element.getKey(), schemaPointer + "/" + "properties/" + element.getKey(),
						operation);
			}
		}
	}

	private void processProperty(JsonNode allData, String elementKey, JsonNode elementValue, JsonNode rootSchema,
			String path, String schemaPointer, EncryptionOperations operation) {

		final JsonNode ref = elementValue.path("$ref");
		if (!ref.isMissingNode()) {
			final String refString = ref.asText();
			final JsonNode referencedElement = getReferencedJsonNode(refString, rootSchema);
			final String newSchemaPointer = refString.substring(refString.lastIndexOf("#/")).substring(1);
			processProperties(allData, referencedElement, rootSchema, path, newSchemaPointer, operation);
		} else {
			final JsonNode oneOf = elementValue.path("oneOf");
			if (!oneOf.isMissingNode()) {
				// only one of the schemas is valid for the property
				if (oneOf.isArray()) {
					final Iterator<JsonNode> miniSchemas = oneOf.elements();
					final JsonNode miniData = getReferencedJsonNode(path, allData);
					boolean notFound = true;
					while (notFound && miniSchemas.hasNext()) {
						try {
							final JsonNode miniSchema = miniSchemas.next();
							final JsonSchema schema = factory.getJsonSchema(rootSchema, schemaPointer);
							final ProcessingReport report = schema.validate(miniData);
							if (report.isSuccess()) {
								notFound = false;

								processProperty(allData, elementKey, miniSchema, rootSchema, path, schemaPointer,
										operation);
							}
						} catch (final ProcessingException e) {
							// if it is not the valid schema it must be ignored
							log.trace("Mini Schema skipped", e);
						}
					}
				}
			} else {
				final JsonNode allOf = elementValue.path("allOf");
				final JsonNode anyOf = elementValue.path("anyOf");
				Iterator<JsonNode> miniSchemas = null;
				if (!anyOf.isMissingNode()) {
					if (anyOf.isArray()) {
						miniSchemas = anyOf.elements();
					}
				} else if (!allOf.isMissingNode()) {
					if (allOf.isArray()) {
						miniSchemas = allOf.elements();
					}
				}

				if (miniSchemas != null) {
					final JsonNode miniData = getReferencedJsonNode(path, allData);
					while (miniSchemas.hasNext()) {
						try {
							final JsonNode miniSchema = miniSchemas.next();
							final JsonSchema schema = factory.getJsonSchema(rootSchema, schemaPointer);
							final ProcessingReport report = schema.validate(miniData);
							if (report.isSuccess()) {
								processProperty(allData, elementKey, miniSchema, rootSchema, path, schemaPointer,
										operation);
							}
						} catch (final ProcessingException e) {
							// if it is not the valid schema it must be ignored
							log.trace("Mini Schema skipped", e);
						}
					}
				} else {
					final JsonNode encrypt = elementValue.path(ENCRYPT_PROPERTY);
					if (encrypt.asBoolean()) {
						final JsonNode data = getReferencedJsonNode(path, allData);
						final String dataToProcess = data.asText();
						String dataProcessed = null;
						try {
							switch (operation) {
							case ENCRYPT:
								dataProcessed = BasicEncryption.encrypt(KEY, INIT_VECTOR, dataToProcess);
								break;
							case DECRYPT:
								dataProcessed = BasicEncryption.decrypt(KEY, INIT_VECTOR, dataToProcess);
								break;

							default:
								throw new IllegalArgumentException("Operation not soported");
							}
							final String propertyPath = path.substring(0, path.lastIndexOf("/"));
							final JsonNode originalData = getReferencedJsonNode(propertyPath, allData);
							((ObjectNode) originalData).put(elementKey, dataProcessed);
						} catch (final Exception e) {
							log.error("Error in encrypting data: " + e.getMessage());
							throw new RuntimeException(e);
						}
					} else {
						processProperties(allData, elementValue, rootSchema, path, schemaPointer, operation);
					}
				}
			}
		}
	}

	private JsonNode getReferencedJsonNode(String ref, JsonNode root) {
		final String[] path = ref.split("/");
		assert path[0].equals("#");
		JsonNode referecedElement = root;

		for (int i = 1; i < path.length; i++) {
			referecedElement = referecedElement.path(path[i]);
		}

		return referecedElement;
	}

	@Override
	public List<String> preProcessInsertData(OperationModel operationModel)
			throws DataSchemaValidationException, IOException {
		final String ontologyName = operationModel.getOntologyName();
		final Ontology ontology = ontologyRepository.findByIdentification(ontologyName);
		final JsonNode dataNode = objectMapper.readTree(operationModel.getBody());
		final List<String> encryptedData = new ArrayList<>();
		if (dataNode.isArray()) {
			for (final JsonNode instance : (ArrayNode) dataNode) {
				checkOntologySchemaCompliance(instance, ontology);
				try {
					final String bodyWithDataContext = addContextData(operationModel, instance);
					final String encryptedDataInBODY = encryptionOperation(bodyWithDataContext, ontology,
							EncryptionOperations.ENCRYPT);
					encryptedData.add(encryptedDataInBODY);
				} catch (final IOException e) {
					throw new RuntimeException(JSON_ERROR, e);
				}
			}
		} else {
			checkOntologySchemaCompliance(dataNode, ontology);
			try {
				final String bodyWithDataContext = addContextData(operationModel, null);
				final String encryptedDataInBODY = encryptionOperation(bodyWithDataContext, ontology,
						EncryptionOperations.ENCRYPT);
				encryptedData.add(encryptedDataInBODY);
			} catch (final IOException e) {
				throw new RuntimeException(JSON_ERROR, e);
			}

		}
		return encryptedData;

	}

	@Override
	public String decrypt(String data, String ontologyName, String user)
			throws OntologyDataUnauthorizedException, OntologyDataJsonProblemException {
		final Ontology ontology = ontologyRepository.findByIdentification(ontologyName);
		if (ontology.getUser().getUserId().equals(user)) {
			try {
				return encryptionOperation(data, ontology, EncryptionOperations.DECRYPT);
			} catch (final IOException e) {
				throw new OntologyDataJsonProblemException(JSON_ERROR, e);
			}
		} else {
			throw new OntologyDataUnauthorizedException("Only the owner can decrypt data");
		}
	}

	@Override
	public void checkTitleCaseSchema(String jsonSchema) throws OntologyDataJsonProblemException {
		try {
			final JsonNode rootSchema = objectMapper.readTree(jsonSchema);

			final Iterator<Entry<String, JsonNode>> elements = rootSchema.path(PROP_STR).fields();

			elements.forEachRemaining(e -> {

				processPropertiesForTitleCase(e.getKey(), e.getValue(), rootSchema, "/" + PROP_STR);

			});

		} catch (final IOException e) {
			throw new OntologyDataJsonProblemException("Schema is not json");
		}
	}

	public void processPropertiesForTitleCase(String field, JsonNode value, JsonNode root, String pointer)
			throws OntologyDataJsonProblemException {
		if (!value.path("$ref").isMissingNode()) {
			final String ref = value.path("$ref").asText();
			final String newPointer = ref.substring(ref.lastIndexOf("#/")).substring(1);
			root.at(newPointer + "/" + PROP_STR).fields().forEachRemaining(e -> {

				processPropertiesForTitleCase(e.getKey(), e.getValue(), root, newPointer);

			});
		} else {
			// if all field is UPPER is not a exception
			if (!field.toUpperCase().equals(field) && Character.isUpperCase(field.charAt(0)))
				throw new OntologyDataJsonProblemException("Properties can not start with Upper case : " + field);
			if (!value.path("type").isMissingNode()) {
				final String type = value.path("type").asText();
				if (type.equalsIgnoreCase("object")) {
					final String newPointer = pointer + "/" + field + "/" + PROP_STR;
					root.at(newPointer).fields().forEachRemaining(e -> {

						processPropertiesForTitleCase(e.getKey(), e.getValue(), root, newPointer);

					});

				} else if (type.equalsIgnoreCase("array")) {
					final String newPointer = pointer + "/" + field + "/items";
					root.at(newPointer).elements().forEachRemaining(n -> {
						if (!n.path(PROP_STR).isMissingNode()) {
							n.path(PROP_STR).fields().forEachRemaining(e -> {
								processPropertiesForTitleCase(e.getKey(), e.getValue(), root, newPointer);
							});
						}

					});
				}
			}
		}
	}

	@Override
	public void checkRequiredFields(String dbJsonSchema, String newJsonSchema) throws OntologyDataJsonProblemException {

		try {
			final JsonNode rootNew = objectMapper.readTree(newJsonSchema);
			final JsonNode rootDb = objectMapper.readTree(dbJsonSchema);
			iteratePropertiesRequired(rootDb, rootNew);
		} catch (final IOException e) {
			throw new OntologyDataJsonProblemException("Not valid json schema");
		}

	}

	public void proccessRequiredProperties(JsonNode oldSchema, JsonNode newSchema) {
		if (!oldSchema.path(REQ_STR).isMissingNode()) {
			JsonNode required = oldSchema.path(REQ_STR);
			final ArrayList<String> properties = new ArrayList<>();
			required.elements().forEachRemaining(n -> properties.add(n.asText()));
			required = newSchema.path(REQ_STR);
			final ArrayList<String> propertiesNew = new ArrayList<>();
			required.elements().forEachRemaining(n -> propertiesNew.add(n.asText()));
			if (!properties.equals(propertiesNew))
				throw new OntologyDataJsonProblemException(REQ_PROP_STR);

		} else if (!newSchema.path(REQ_STR).isMissingNode())
			throw new OntologyDataJsonProblemException(REQ_PROP_STR);
	}

	public void iteratePropertiesRequired(JsonNode oldSchema, JsonNode newSchema) {
		final String ref = refJsonSchema(oldSchema);
		String pointer = "/" + PROP_STR;
		if (!StringUtils.isEmpty(ref))
			pointer = ref + pointer;
		if (!oldSchema.at(ref + "/required").isMissingNode()) {
			proccessRequiredProperties(oldSchema.at(ref), newSchema.at(ref));
		} else if (!newSchema.at(ref + "/required").isMissingNode())
			throw new OntologyDataJsonProblemException(REQ_PROP_STR);

		if (!oldSchema.at(pointer).isMissingNode()) {
			final JsonNode properties = oldSchema.at(pointer);
			final JsonNode propertiesNew = newSchema.at(pointer);
			final Iterator<Entry<String, JsonNode>> elements = properties.fields();
			while (elements.hasNext()) {
				final Entry<String, JsonNode> e = elements.next();
				if (!e.getValue().path(REQ_STR).isMissingNode()) {
					final String path = e.getKey();
					processSingleProperty4Required(e.getValue(), propertiesNew.path(path));

				}
			}
		}
	}

	public void processSingleProperty4Required(JsonNode oldSchema, JsonNode newSchema) {
		proccessRequiredProperties(oldSchema, newSchema);
		if (!oldSchema.path("type").isMissingNode() && oldSchema.path("type").asText().equals("object")) {
			final JsonNode properties = oldSchema.path(PROP_STR);
			final JsonNode propertiesNew = newSchema.path(PROP_STR);
			final Iterator<Entry<String, JsonNode>> elements = properties.fields();
			while (elements.hasNext()) {
				final Entry<String, JsonNode> e = elements.next();
				if (!e.getValue().path(REQ_STR).isMissingNode()) {
					final String path = e.getKey();
					processSingleProperty4Required(e.getValue(), propertiesNew.path(path));

				}
			}
			// properties.fields().forEachRemaining(e -> {
			// if (!e.getValue().path(REQ_STR).isMissingNode()) {
			// final String path = "properties/" + e.getKey();
			// processSingleProperty4Required(e.getValue(), propertiesNew.path(path));
			// // proccessRequiredProperties(oldSchema.path(path), newSchema.path(path));
			//
			// }
			// });
		}
	}

	@Override
	public String refJsonSchema(JsonNode schema) {
		final Iterator<Entry<String, JsonNode>> elements = schema.path(PROP_STR).fields();
		String reference = "";
		while (elements.hasNext()) {
			final Entry<String, JsonNode> entry = elements.next();
			if (!entry.getValue().path("$ref").isMissingNode()) {
				final String ref = entry.getValue().path("$ref").asText();
				reference = ref.substring(ref.lastIndexOf("#/")).substring(1);
			}
		}
		return reference;
	}

	@Override
	public ProcessingReport reportJsonSchemaValid(String jsonSchema) throws IOException {
		final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
		final SyntaxValidator validator = factory.getSyntaxValidator();
		return validator.validateSchema(objectMapper.readTree(jsonSchema));
	}

	@Override
	public Set<OntologyRelation> getOntologyReferences(String ontologyIdentification) throws IOException {
		final Ontology ontology = ontologyRepository.findByIdentification(ontologyIdentification);
		final Set<OntologyRelation> relations = new TreeSet<>();
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode schemaOrigin = mapper.readTree(ontology.getJsonSchema());
		if (!schemaOrigin.path("_references").isMissingNode()) {
			schemaOrigin.path("_references").forEach(r -> {
				String srcAtt = r.path("self").asText();
				String targetAtt = r.path("target").asText().split("#")[1];
				final String targetOntology = r.path("target").asText().split("#")[0].replace("ontologies/schema/", "");
				final Ontology target = ontologyRepository.findByIdentification(targetOntology);
				final String refOrigin = refJsonSchema(schemaOrigin);
				if (!"".equals(refOrigin))
					srcAtt = srcAtt.replaceAll(refOrigin.replace("/", ""), schemaOrigin.at("/required/0").asText());
				if (target == null)
					throw new RuntimeException(
							"Target ontology of " + ontology.getIdentification() + " not found on platform");
				try {
					final JsonNode schemaTarget = mapper.readTree(target.getJsonSchema());
					final String refTarget = refJsonSchema(schemaTarget);
					if (!"".equals(refTarget))
						targetAtt = targetAtt.replaceAll(refTarget.replace("/", ""),
								schemaTarget.at("/required/0").asText());
				} catch (final IOException e) {
					log.debug("No $ref");
				}
				targetAtt = targetAtt.replaceAll(PROP_STR + ".", "").replaceAll("items.", "").replaceAll(".items", "");
				srcAtt = srcAtt.replaceAll(PROP_STR + ".", "").replaceAll("items.", "").replaceAll(".items", "");
				relations.add(new OntologyRelation(ontology.getIdentification(), target.getIdentification(), srcAtt,
						targetAtt));

			});

		}
		return relations;
	}

	@Override
	public Map<String, String> getOntologyPropertiesWithPath4Type(String ontologyIdentification, String type) {
		final Map<String, String> map = new HashMap<>();
		final Ontology ontology = ontologyRepository.findByIdentification(ontologyIdentification);
		if (ontology != null) {
			final ObjectMapper mapper = new ObjectMapper();
			try {
				final JsonNode schema = mapper.readTree(ontology.getJsonSchema());
				final String reference = refJsonSchema(schema);
				final String parentNode = reference.equals("") ? PROP_STR + "."
						: reference.replace("/", "") + ".properties.";
				final String path = reference.equals("") ? "/" + PROP_STR : reference + "/" + PROP_STR;
				final JsonNode properties = schema.at(path);
				properties.fields().forEachRemaining(e -> {
					if (e.getValue().path("type").asText().equals(type))
						map.put(e.getKey(), parentNode + e.getKey());
				});
			} catch (final IOException e) {
				log.error("Could not read json schema for properties");
			}
		}
		return map;
	}
}
