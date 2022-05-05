package com.eventstore.dbclient;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemConfig {
  private static final SystemConfig INSTANCE = new SystemConfig();
  private Configuration configuration;

  private SystemConfig() {
    try {
      Configurations configs = new Configurations();
      configuration = configs.xml(ClassLoader.getSystemClassLoader().getResource("eventstoredb-client.xml"));
    } catch (ConfigurationException e) {
      Logger logger = LoggerFactory.getLogger(SystemConfig.class);
      logger.error("An error occurred trying to retrieve configurations.", e);
    }
  }

  public static SystemConfig getSystemConfig() {
    return INSTANCE;
  }

  public Configuration getConfig(String propertyName){
    return configuration.subset(propertyName);
  }

  public boolean hasPath(String path) {
    return !getConfig(path).isEmpty();
  }
}
