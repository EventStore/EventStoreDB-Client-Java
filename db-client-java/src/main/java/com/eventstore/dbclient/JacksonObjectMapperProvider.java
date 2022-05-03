package com.eventstore.dbclient;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
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
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry of shared ObjectMapper instances, each with its unique bindingName.
 */
public class JacksonObjectMapperProvider {
  private final Logger logger = LoggerFactory.getLogger(JacksonObjectMapperProvider.class);
  private final Map<String, ObjectMapper> objectMappers = new ConcurrentHashMap<>();
  private final JacksonObjectMapperFactory objectMapperFactory;

  public JacksonObjectMapperProvider(JacksonObjectMapperFactory objectMapperFactory) {
    this.objectMapperFactory = objectMapperFactory;
  }

  /**
   * Returns an existing Jackson 'ObjectMapper' that was created previously with this method, or
   * creates a new instance.
   *
   * The 'ObjectMapper' is created with sensible defaults and modules configured
   * in 'serialization.jackson.modules'.
   *
   * The returned 'ObjectMapper' must not be modified, because it may already be in use and such
   * modifications are not thread-safe.
   *
   * @param bindingName name of this 'ObjectMapper'
   * @param jsonFactory optional 'JsonFactory' such as 'CBORFactory', for plain JSON null (defaults)
   *                    can be used
   */
  public ObjectMapper getOrCreate(String bindingName, JsonFactory jsonFactory) {
    return objectMappers.computeIfAbsent(bindingName, objectMapper -> create(bindingName, jsonFactory));
  }

  /**
   * Creates a new instance of a Jackson 'ObjectMapper' with sensible defaults and modules configured
   * in 'serialization.jackson.jackson-modules'.
   *
   * @param bindingName name of this 'ObjectMapper'
   * @param jsonFactory optional 'JsonFactory' such as 'CBORFactory', for plain JSON null (defaults)
   *                    can be used
   * @see JacksonObjectMapperProvider#getOrCreate(String, JsonFactory)
   */
  private ObjectMapper create(String bindingName, JsonFactory jsonFactory) {
    Configuration configuration = this.configForBinding(bindingName);
    return createObjectMapper(bindingName, jsonFactory, configuration);
  }

  private Configuration configForBinding(String bindingName) {
    String basePath = "serialization.jackson";
    SystemConfig systemConfig = SystemConfig.getSystemConfig();
    Configuration baseConf = systemConfig.getConfig(basePath);
    if (systemConfig.hasPath(basePath + bindingName)) {
      return systemConfig.getConfig(basePath + bindingName);
    }
    return baseConf;
  }

  private ObjectMapper createObjectMapper(
          String bindingName,
          JsonFactory jsonFactory,
          Configuration configuration) {

    JsonFactory configuredJsonFactory = createJsonFactory(bindingName, objectMapperFactory, jsonFactory, configuration);
    ObjectMapper mapper = objectMapperFactory.newObjectMapper(configuredJsonFactory);

    configureObjectMapperFeatures(bindingName, mapper, configuration);
    configureObjectMapperModules(bindingName, mapper, configuration);
    configureObjectVisibility(bindingName, mapper, configuration);
    return mapper;
  }

  private JsonFactory createJsonFactory(
          String bindingName,
          JacksonObjectMapperFactory objectMapperFactory,
          JsonFactory baseJsonFactory,
          Configuration configuration) {

    JsonFactory jsonFactory = (baseJsonFactory != null) ? baseJsonFactory : new JsonFactoryBuilder().build();

    // Stream Read Features
    Map<StreamReadFeature, Boolean> configuredStreamReadFeatures = getConfiguredFeatures(configuration, "stream.read-features", StreamReadFeature::valueOf, Boolean::parseBoolean);

    Map<StreamReadFeature, Boolean> streamReadFeatures = objectMapperFactory.overrideConfiguredStreamReadFeatures(bindingName, configuredStreamReadFeatures);

    streamReadFeatures.forEach((feature, value) -> jsonFactory.configure(feature.mappedFeature(), value));

    // Stream Write Features
    Map<StreamWriteFeature, Boolean> configuredStreamWriteFeatures = getConfiguredFeatures(configuration, "stream.write-features", StreamWriteFeature::valueOf, Boolean::parseBoolean);

    Map<StreamWriteFeature, Boolean> streamWriteFeatures = objectMapperFactory.overrideConfiguredStreamWriteFeatures(bindingName, configuredStreamWriteFeatures);

    streamWriteFeatures.forEach((feature, value) -> jsonFactory.configure(feature.mappedFeature(), value));

    // JSON Read Features
    Map<JsonReadFeature, Boolean> configuredJsonReadFeatures = getConfiguredFeatures(configuration, "json.read-features", JsonReadFeature::valueOf, Boolean::parseBoolean);

    Map<JsonReadFeature, Boolean> jsonReadFeatures = objectMapperFactory.overrideConfiguredJsonReadFeatures(bindingName, configuredJsonReadFeatures);

    jsonReadFeatures.forEach((feature, value) -> jsonFactory.configure(feature.mappedFeature(), value));

    // Json Write Features
    Map<JsonWriteFeature, Boolean> configuredJsonWriteFeatures = getConfiguredFeatures(configuration, "json.write-features", JsonWriteFeature::valueOf, Boolean::parseBoolean);

    Map<JsonWriteFeature, Boolean> jsonWriteFeatures = objectMapperFactory.overrideConfiguredJsonWriteFeatures(bindingName, configuredJsonWriteFeatures);

    jsonWriteFeatures.forEach((feature, value) -> jsonFactory.configure(feature.mappedFeature(), value));

    return jsonFactory;
  }

  private <T, U> Map<T, U> getConfiguredFeatures(Configuration configuration, String keyword, Function<String, T> onFeature, Function<String, U> onValue) {
    return new ConfigurationMap(configuration.subset(keyword))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                    e -> onFeature.apply(e.getKey().toString()),
                    e -> onValue.apply(e.getValue().toString())));
  }

  private void configureObjectMapperFeatures(String bindingName, ObjectMapper objectMapper, Configuration configuration) {
    // Serialization Features
    Map<SerializationFeature, Boolean> configuredSerializationFeatures = getConfiguredFeatures(configuration, "serialization-features", SerializationFeature::valueOf, Boolean::parseBoolean);

    Map<SerializationFeature, Boolean> serializationFeatures = objectMapperFactory.overrideConfiguredSerializationFeatures(bindingName, configuredSerializationFeatures);

    serializationFeatures.forEach(objectMapper::configure);

    // Deserialization Features
    Map<DeserializationFeature, Boolean> configuredDeserializationFeatures = getConfiguredFeatures(configuration, "deserialization-features", DeserializationFeature::valueOf, Boolean::parseBoolean);

    Map<DeserializationFeature, Boolean> deserializationFeatures = objectMapperFactory.overrideConfiguredDeserializationFeatures(bindingName, configuredDeserializationFeatures);

    deserializationFeatures.forEach(objectMapper::configure);

    // Mapper Features
    Map<MapperFeature, Boolean> configuredMapperFeatures = getConfiguredFeatures(configuration, "mapper-features", MapperFeature::valueOf, Boolean::parseBoolean);

    Map<MapperFeature, Boolean> mapperFeatures = objectMapperFactory.overrideConfiguredMapperFeatures(bindingName, configuredMapperFeatures);

    mapperFeatures.forEach(objectMapper::configure);

    // Json Parser Features
    Map<JsonParser.Feature, Boolean> configuredJsonParserFeatures = getConfiguredFeatures(configuration, "json.parser-features", JsonParser.Feature::valueOf, Boolean::parseBoolean);

    Map<JsonParser.Feature, Boolean> jsonParserFeatures = objectMapperFactory.overrideConfiguredJsonParserFeatures(bindingName, configuredJsonParserFeatures);

    jsonParserFeatures.forEach(objectMapper::configure);

    // Json Generator Features
    Map<JsonGenerator.Feature, Boolean> configuredJsonGeneratorFeatures = getConfiguredFeatures(configuration, "json.generator-features", JsonGenerator.Feature::valueOf, Boolean::parseBoolean);

    Map<JsonGenerator.Feature, Boolean> jsonGeneratorFeatures = objectMapperFactory.overrideConfiguredJsonGeneratorFeatures(bindingName, configuredJsonGeneratorFeatures);

    jsonGeneratorFeatures.forEach(objectMapper::configure);
  }

  private void configureObjectMapperModules(
          String bindingName,
          ObjectMapper objectMapper,
          Configuration configuration) {

    List<Object> config = configuration.getList("modules.module");

    List<Module> configuredModules = config.stream()
            .map(Object::toString)
            .map((String className) -> {
              try {
                return (Module) Class.forName(className)
                        .getConstructor()
                        .newInstance();
              } catch (Exception e) {
                logger.error("Could not load configured Jackson module {}, " +
                        "please verify classpath dependencies or amend the configuration " +
                        "[serialization.jackson-modules]. Continuing without this module.", className, e);
              }
              return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    List<Module> modules = objectMapperFactory.overrideConfiguredModules(bindingName, configuredModules);

    modules.forEach(module -> {
      objectMapper.registerModule(module);
      logger.debug("Registered Jackson module [{}]", module.getClass().getName());
    });
  }

  private void configureObjectVisibility(String bindingName, ObjectMapper objectMapper, Configuration configuration) {
    // Object Visibility
    Map<PropertyAccessor, Visibility> configuredVisibility = getConfiguredFeatures(configuration, "visibility", PropertyAccessor::valueOf, Visibility::valueOf);

    Map<PropertyAccessor, Visibility> visibility = objectMapperFactory.overrideConfiguredVisibility(bindingName, configuredVisibility);

    visibility.forEach(objectMapper::setVisibility);
  }
}
