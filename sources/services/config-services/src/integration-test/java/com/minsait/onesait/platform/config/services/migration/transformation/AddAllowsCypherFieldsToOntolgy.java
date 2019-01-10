package com.minsait.onesait.platform.config.services.migration.transformation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import de.galan.verjson.step.transformation.Transformation;
import static de.galan.verjson.util.Transformations.*;

import java.util.function.Consumer;


public class AddAllowsCypherFieldsToOntolgy extends Transformation{

	@Override
	protected void transform(JsonNode node) {
		JsonNode nodeAllData = node.get("allData");
		
		if (nodeAllData.isArray()) {
			ArrayNode arrayAllData = (ArrayNode) nodeAllData;
			arrayAllData.forEach(consumerData);
			
		} else {
			throw new IllegalArgumentException("The json provided has an invalid format");
		}
	}
	
	private Consumer<JsonNode> consumerData = new Consumer<JsonNode>() {
		@Override
		public void accept(JsonNode node) {
			JsonNode jsonNode = node.get("class");
			String className = jsonNode.asText();
			switch (className) {
			case "com.minsait.onesait.platform.config.model.Ontology":
				JsonNode ontologiesNode = node.get("instances");
				ArrayNode arrayOntologies = (ArrayNode) ontologiesNode;
				arrayOntologies.forEach(transformOntology);
				break;

			default:
				break;
			}
		}
	};
	
	private Consumer<? super JsonNode> transformOntology = new Consumer<JsonNode>() {
		@Override
		public void accept(JsonNode node) {
			JsonNode jsonNode = node.get("data");
			obj(jsonNode).put("allowsCypherFields", false);
		}
	};

}
