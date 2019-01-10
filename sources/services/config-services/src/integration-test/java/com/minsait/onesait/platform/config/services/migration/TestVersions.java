package com.minsait.onesait.platform.config.services.migration;

import com.minsait.onesait.platform.config.services.migration.transformation.AddAllowsCypherFieldsToOntolgy;

import de.galan.verjson.core.Versions;

public class TestVersions extends Versions{

	@Override
	public void configure() {
		setIncludeTimestamp(false);
		registerSerializer(new DataFromDBJsonSerializer());
		registerDeserializer(new DataFromDBJsonDeserializer());
		add(1L, new AddAllowsCypherFieldsToOntolgy());
	}
}
