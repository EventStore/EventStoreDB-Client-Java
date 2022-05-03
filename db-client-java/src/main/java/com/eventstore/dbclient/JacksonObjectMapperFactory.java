package com.eventstore.dbclient;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.StreamWriteFeature;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

public interface JacksonObjectMapperFactory {
  String NAME = JacksonObjectMapperFactory.class.getSimpleName();

  default ObjectMapper newObjectMapper(JsonFactory jsonFactory) {
    return JsonMapper.builder(jsonFactory).build();
  }

  Map<StreamReadFeature, Boolean> overrideConfiguredStreamReadFeatures(String bindingName, Map<StreamReadFeature, Boolean> features);

  Map<StreamWriteFeature, Boolean> overrideConfiguredStreamWriteFeatures(String bindingName, Map<StreamWriteFeature, Boolean> features);

  Map<JsonReadFeature, Boolean> overrideConfiguredJsonReadFeatures(String bindingName, Map<JsonReadFeature, Boolean> features);

  Map<JsonWriteFeature, Boolean> overrideConfiguredJsonWriteFeatures(String bindingName, Map<JsonWriteFeature, Boolean> features);

  Map<SerializationFeature, Boolean> overrideConfiguredSerializationFeatures(String bindingName, Map<SerializationFeature, Boolean> features);

  Map<DeserializationFeature, Boolean> overrideConfiguredDeserializationFeatures(String bindingName, Map<DeserializationFeature, Boolean> features);

  Map<MapperFeature, Boolean> overrideConfiguredMapperFeatures(String bindingName, Map<MapperFeature, Boolean> features);

  Map<JsonParser.Feature, Boolean> overrideConfiguredJsonParserFeatures(String bindingName, Map<JsonParser.Feature, Boolean> features);

  Map<JsonGenerator.Feature, Boolean> overrideConfiguredJsonGeneratorFeatures(String bindingName, Map<JsonGenerator.Feature, Boolean> features);

  List<Module> overrideConfiguredModules(String bindingName, List<Module> modules);

  Map<PropertyAccessor, Visibility> overrideConfiguredVisibility(String bindingName, Map<PropertyAccessor, Visibility> configuredVisibility);
}
