package com.eventstore.dbclient.samples;

import com.eventstore.dbclient.JacksonObjectMapperFactory;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.StreamWriteFeature;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JacksonObjectMapperFactoryTestImpl implements JacksonObjectMapperFactory {

  @Override
  public Map<StreamReadFeature, Boolean> overrideConfiguredStreamReadFeatures(String bindingName, Map<StreamReadFeature, Boolean> features) {
    return features;
  }

  @Override
  public Map<StreamWriteFeature, Boolean> overrideConfiguredStreamWriteFeatures(String bindingName, Map<StreamWriteFeature, Boolean> features) {
    return features;
  }

  @Override
  public Map<JsonReadFeature, Boolean> overrideConfiguredJsonReadFeatures(String bindingName, Map<JsonReadFeature, Boolean> features) {
    return features;
  }

  @Override
  public Map<JsonWriteFeature, Boolean> overrideConfiguredJsonWriteFeatures(String bindingName, Map<JsonWriteFeature, Boolean> features) {
    return features;
  }

  @Override
  public Map<SerializationFeature, Boolean> overrideConfiguredSerializationFeatures(String bindingName, Map<SerializationFeature, Boolean> features) {
    return features;
  }

  @Override
  public Map<DeserializationFeature, Boolean> overrideConfiguredDeserializationFeatures(String bindingName, Map<DeserializationFeature, Boolean> features) {
    return features;
  }

  @Override
  public Map<MapperFeature, Boolean> overrideConfiguredMapperFeatures(String bindingName, Map<MapperFeature, Boolean> features) {
    return features;
  }

  @Override
  public Map<Feature, Boolean> overrideConfiguredJsonParserFeatures(String bindingName, Map<Feature, Boolean> features) {
    return features;
  }

  @Override
  public Map<JsonGenerator.Feature, Boolean> overrideConfiguredJsonGeneratorFeatures(String bindingName, Map<JsonGenerator.Feature, Boolean> features) {
    return features;
  }

  @Override
  public List<Module> overrideConfiguredModules(String bindingName, List<Module> modules) {
    return Collections.emptyList();
  }

  @Override
  public Map<PropertyAccessor, Visibility> overrideConfiguredVisibility(String bindingName, Map<PropertyAccessor, Visibility> configuredVisibility) {
    return configuredVisibility;
  }
}
