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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry of shared ObjectMapper instances, each with its unique bindingName.
 */
public class JacksonObjectMapperProvider {
  public static final JacksonObjectMapperProvider INSTANCE = new JacksonObjectMapperProvider();
  private final Logger logger = LoggerFactory.getLogger(JacksonObjectMapperProvider.class);
  private final Map<String, ObjectMapper> objectMappers = new ConcurrentHashMap<>();

  private JacksonObjectMapperProvider() {
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

    JacksonObjectMapperFactory objectMapperFactory = Optional.ofNullable(System.getProperty(JacksonObjectMapperFactory.NAME))
            .map(name -> getInstanceFromName(name, JacksonObjectMapperFactory.class))
            .orElse(new JacksonObjectMapperFactoryImpl());

    return createObjectMapper(bindingName, objectMapperFactory, jsonFactory, configuration);
  }

  private Configuration configForBinding(String bindingName) {
    String basePath = "serialization.jackson";
    String bindingPath = basePath + "." + bindingName;
    SystemConfig systemConfig = SystemConfig.getSystemConfig();
    if (systemConfig.hasPath(bindingPath)) {
      return systemConfig.getConfig(bindingPath);
    }
    return systemConfig.getConfig(basePath);
  }

  private <T> T getInstanceFromName(String className, Class<T> clazz) {
    try {
      return clazz.cast(Class.forName(className)
              .getConstructor()
              .newInstance());
    } catch (Exception e) {
      logger.error("Could not load configured Jackson class {}, " +
              "please verify classpath dependencies or amend the configuration " +
              "[serialization.jackson]. Continuing without this class.", className, e);
    }
    return null;
  }

  private ObjectMapper createObjectMapper(
          String bindingName,
          JacksonObjectMapperFactory objectMapperFactory,
          JsonFactory jsonFactory,
          Configuration configuration) {

    JsonFactory configuredJsonFactory = createJsonFactory(bindingName, objectMapperFactory, jsonFactory, configuration);
    ObjectMapper mapper = objectMapperFactory.newObjectMapper(configuredJsonFactory);

    configureObjectMapperFeatures(bindingName, mapper, objectMapperFactory, configuration);
    configureObjectMapperModules(bindingName, mapper, objectMapperFactory, configuration);
    configureObjectVisibility(bindingName, mapper, objectMapperFactory, configuration);
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
            .filter(e -> StringUtils.isNotBlank(e.getKey().toString()))
            .collect(Collectors.toMap(
                    e -> onFeature.apply(e.getKey().toString()),
                    e -> onValue.apply(e.getValue().toString())));
  }

  @SuppressWarnings("Deprecated")
  private void configureObjectMapperFeatures(String bindingName, ObjectMapper objectMapper, JacksonObjectMapperFactory objectMapperFactory, Configuration configuration) {
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
          JacksonObjectMapperFactory objectMapperFactory, Configuration configuration) {

    List<Object> config = configuration.getList("modules.module");

    List<Module> configuredModules = config.stream()
            .map(Object::toString)
            .map((String className) -> getInstanceFromName(className, Module.class))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    List<Module> modules = objectMapperFactory.overrideConfiguredModules(bindingName, configuredModules);

    modules.forEach(module -> {
      objectMapper.registerModule(module);
      logger.debug("Registered Jackson module [{}]", module.getClass().getName());
    });
  }

  private void configureObjectVisibility(String bindingName, ObjectMapper objectMapper, JacksonObjectMapperFactory objectMapperFactory, Configuration configuration) {
    // Object Visibility
    Map<PropertyAccessor, Visibility> configuredVisibility = getConfiguredFeatures(configuration, "visibility", PropertyAccessor::valueOf, Visibility::valueOf);

    Map<PropertyAccessor, Visibility> visibility = objectMapperFactory.overrideConfiguredVisibility(bindingName, configuredVisibility);

    visibility.forEach(objectMapper::setVisibility);
  }
}
