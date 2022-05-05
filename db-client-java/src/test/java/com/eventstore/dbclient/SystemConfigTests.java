package com.eventstore.dbclient;

import org.apache.commons.configuration2.Configuration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemConfigTests {

  @Test
  void testGetConfiguredKeyWhenGetSpecificSubsetConfigFromSystemProperties() {
    SystemConfig systemConfig = SystemConfig.getSystemConfig();

    Configuration config = systemConfig.getConfig("serialization.jackson");

    assertNotNull(config);
    assertTrue(config.containsKey("modules.module"));
  }

  @Test
  void testGetTrueWhenAskForAConfiguredPathFromAValidBasePathInSystemProperties() {
    SystemConfig systemConfig = SystemConfig.getSystemConfig();
    String basePath = "serialization.jackson";

    assertTrue(systemConfig.hasPath(basePath));
  }

  @Test
  void testGetFalseWhenAskForAConfiguredPathFromAnInvalidBasePathInSystemProperties() {
    SystemConfig systemConfig = SystemConfig.getSystemConfig();

    assertFalse(systemConfig.hasPath("not-configured-path"));
  }
}