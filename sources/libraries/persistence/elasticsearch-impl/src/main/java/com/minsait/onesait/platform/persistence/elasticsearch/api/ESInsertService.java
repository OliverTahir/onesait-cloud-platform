/**
 * Copyright minsait by Indra Sistemas, S.A.
 * 2013-2018 SPAIN
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
package com.minsait.onesait.platform.persistence.elasticsearch.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minsait.onesait.platform.persistence.util.BulkWriteResult;
import com.minsait.onesait.platform.persistence.util.JSONPersistenceUtilsElasticSearch;

import io.searchbox.core.Bulk;
import io.searchbox.core.BulkResult;
import io.searchbox.core.Index;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ESInsertService {

	@Autowired
	ESBaseApi connector;
	
	private static final String GEOMERY_STR = "geometry";

	private String fixPosibleNonCapitalizedGeometryPoint(String s) {
		try {
			final JsonObject o = new JsonParser().parse(s).getAsJsonObject();
			final JsonObject geometry = (JsonObject) o.get(GEOMERY_STR);
			final JsonElement type = geometry.get("type");
			final String value = type.getAsString();
			geometry.addProperty("type", value.toLowerCase());
			o.remove(GEOMERY_STR);
			o.add(GEOMERY_STR, geometry);
			return o.toString();

		} catch (final Exception e) {
		}
		return s;

	}

	private String fixPosibleDollarDates(String s, String schema, String index) {
		if (s.contains("$date")) {
			try {
				final String elasticSchema = JSONPersistenceUtilsElasticSearch
						.getElasticSearchSchemaFromJSONSchema(index, schema);
				final JsonObject instance = new JsonParser().parse(s).getAsJsonObject();
				final JsonObject elasticSchemaObject = new JsonParser().parse(elasticSchema).getAsJsonObject();
				final JsonObject properties = elasticSchemaObject.get(index).getAsJsonObject().get("properties")
						.getAsJsonObject();
				properties.entrySet().forEach(e -> {
					if (e.getValue().getAsJsonObject().get("type").getAsString().equals("date")) {
						final JsonObject date = instance.get(e.getKey()).getAsJsonObject();
						instance.remove(e.getKey());
						instance.add(e.getKey(), date.get("$date"));
					}

				});
				return instance.toString();

			} catch (final JSONException e1) {

			}

		}
		return s;

	}

	public List<BulkWriteResult> load(String index, String type, List<String> jsonDoc, String jsonSchema) {

		final List<BulkWriteResult> listResult = new ArrayList<BulkWriteResult>();

		final List<Index> list = new ArrayList<Index>();
		for (String s : jsonDoc) {

			s = s.replaceAll("\\n", "");
			s = s.replaceAll("\\r", "");

			s = fixPosibleNonCapitalizedGeometryPoint(s);
			s = fixPosibleDollarDates(s, jsonSchema, index);
			final Index i = new Index.Builder(s).index(index).type(type).build();
			list.add(i);
		}

		final Bulk bulk = new Bulk.Builder().addAction(list).build();
		BulkResult result;
		try {
			result = connector.getHttpClient().execute(bulk);
			final JsonArray object = result.getJsonObject().get("items").getAsJsonArray();

			for (int i = 0; i < object.size(); i++) {
				final JsonElement element = object.get(i);
				final JsonObject o = element.getAsJsonObject();
				final JsonObject the = o.get("index").getAsJsonObject();
				final String id = the.get("_id").getAsString();
				final String created = the.get("result").getAsString();

				final BulkWriteResult bulkr = new BulkWriteResult();
				bulkr.setId(id);
				bulkr.setErrorMessage(created);
				bulkr.setOk(true);
				listResult.add(bulkr);

			}
		} catch (final Exception e) {
			log.error("Error Loading document " + e.getMessage());
		}

		log.info("Documents has been inserted..." + listResult.size());

		return listResult;

	}

	public static String readAllBytes(String filePath) {
		String content = "";
		try {
			content = new String(Files.readAllBytes(Paths.get(filePath)));
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	public static List<String> readLines(File file) throws Exception {
		if (!file.exists()) {
			return new ArrayList<String>();
		}

		final List<String> results = new ArrayList<String>();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file));){

			String line = reader.readLine();
			while (line != null) {
				results.add(line);
				line = reader.readLine();
			}
			return results;
		} catch (final Exception e) {
			return new ArrayList<String>();
		}

	}

}