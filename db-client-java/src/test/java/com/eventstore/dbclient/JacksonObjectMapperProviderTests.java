package com.eventstore.dbclient;

import com.eventstore.dbclient.samples.JacksonObjectMapperFactoryTestImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JacksonObjectMapperProviderTests {

  @Test
  void testGetDefaultModuleConfiguredFromMapperWhenBindingNameDoesNotExist() {
    String defaultModuleConfigured = "com.fasterxml.jackson.datatype.jdk8.Jdk8Module";
    JacksonObjectMapperProvider objectMapperProvider = JacksonObjectMapperProvider.INSTANCE;

    ObjectMapper mapper = objectMapperProvider.getOrCreate("binding-name-not-configured", null);

    assertNotNull(mapper);
    assertTrue(mapper.getRegisteredModuleIds().contains(defaultModuleConfigured));
  }

  @Test
  void testGetConfiguredModuleForBindingFromMapperWhenBindingNameMatch() {
    String configureDefaultModule = "jackson-datatype-jsr310";
    JacksonObjectMapperProvider objectMapperProvider = JacksonObjectMapperProvider.INSTANCE;

    ObjectMapper mapper = objectMapperProvider.getOrCreate("RecordedEvent", null);

    assertNotNull(mapper);
    assertTrue(mapper.getRegisteredModuleIds().contains(configureDefaultModule));
  }

  @Test
  void testMapperIsEqualsWhenTryToGetORCreateMapperWithSameBindingName() {
    JacksonObjectMapperProvider objectMapperProvider = JacksonObjectMapperProvider.INSTANCE;

    ObjectMapper mapper1 = objectMapperProvider.getOrCreate("test-binding-name-same", null);
    ObjectMapper mapper2 = objectMapperProvider.getOrCreate("test-binding-name-same", null);

    assertNotNull(mapper1);
    assertNotNull(mapper2);

    assertEquals(mapper1, mapper2);
  }

  @Test
  void testOverrideConfigurationsProgrammaticallyFromParamConfig() {
    JacksonObjectMapperProvider objectMapperProvider = JacksonObjectMapperProvider.INSTANCE;
    System.setProperty(JacksonObjectMapperFactory.NAME, JacksonObjectMapperFactoryTestImpl.class.getName());

    ObjectMapper mapper = objectMapperProvider.getOrCreate("TestFactory", null);

    assertNotNull(mapper);
    assertTrue(mapper.getRegisteredModuleIds().isEmpty());
  }

  @Test
  void testGetMapperWithDefaultConfigWhenParameterizedConfigIsNotCorrect() {
    String defaultModuleConfigured = "com.fasterxml.jackson.datatype.jdk8.Jdk8Module";
    JacksonObjectMapperProvider objectMapperProvider = JacksonObjectMapperProvider.INSTANCE;
    System.setProperty(JacksonObjectMapperFactory.NAME, "InvalidClass");

    ObjectMapper mapper = objectMapperProvider.getOrCreate("InvalidClass", null);

    assertNotNull(mapper);
    assertTrue(mapper.getRegisteredModuleIds().contains(defaultModuleConfigured));
  }
}
