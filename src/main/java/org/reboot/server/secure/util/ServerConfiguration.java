package org.reboot.server.secure.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.reboot.server.secure.model.PropertyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ServerConfiguration implements Runnable, IServerConfiguration {

  private static Logger log = LoggerFactory.getLogger(ServerConfiguration.class);
  private ScheduledExecutorService executorService;
  private String previouslyKnownMd5;

  private String configFilePath;

  private Map<String, String> properties;

  public static final String CONFIG_FILE_PATH = "config.file.path";

  @Autowired
  public ServerConfiguration() {

    String appConfigFilePath = System.getProperty(CONFIG_FILE_PATH);
    if (StringUtils.isBlank(appConfigFilePath)) {
      throw new RuntimeException(CONFIG_FILE_PATH + "NOT CONFIGURED");
    }

    File file = new File(appConfigFilePath);
    if (!file.exists()) {
      throw new RuntimeException("APP CONFIG FILE NOT EXISTS: " + appConfigFilePath);
    }

    this.configFilePath = appConfigFilePath;
    this.properties = new ConcurrentHashMap<>();
    this.executorService = Executors.newSingleThreadScheduledExecutor();
    this.forceLoadConfigurationAndUpdateFileMd5();
    log.trace("Scheduling md5 verifier at: ", new Date());
    executorService.scheduleWithFixedDelay(this, 10, 10, TimeUnit.SECONDS);
  }

  private void forceLoadConfigurationAndUpdateFileMd5() {
    try (InputStream input = new FileInputStream(this.configFilePath)) {
      Properties prop = new Properties();
      prop.load(input);

      prop.forEach((k, v) -> {
        properties.put(k.toString(), v.toString());
      });

      this.previouslyKnownMd5 = getMd5HexOfConfigFile();
    } catch (IOException ex) {
      ex.printStackTrace();
      throw new RuntimeException(ex);
    }
  }

  private void reloadConfigIfRequired() {
    String recentlyCalculatedMd5 = getMd5HexOfConfigFile();
    log.trace("Previously-Known md5: {} recentlyCalculatedMd5 md5: {}", this.previouslyKnownMd5, recentlyCalculatedMd5);

    if (!StringUtils.equalsIgnoreCase(this.previouslyKnownMd5, recentlyCalculatedMd5)) {
      log.trace("Reloading configuration");
      forceLoadConfigurationAndUpdateFileMd5();
      log.trace("Reloading configuration complete");
    } else {
      log.trace("Reloading configuration not required.");
    }
  }

  private String getMd5HexOfConfigFile() {
    try (InputStream is = Files.newInputStream(Paths.get(this.configFilePath))) {
      String md5 = DigestUtils.md5Hex(is);
      return md5;
    } catch (Exception e) {
      log.trace("Failed to calculate md5: ", e);
      throw new RuntimeException(e);
    }
  }

  public String getProperty(String key) {
    return getProperty(key, false);
  }

  public String getRequiredProperty(String key) {
    return getProperty(key, true);
  }

  public String getProperty(String key, boolean failIfNotConfigured) {
    String value = properties.get(key);
    if (StringUtils.isBlank(value)) {
      log.error("Property value not found for key: {} using empty value", key);
      if (failIfNotConfigured) {
        throw new PropertyNotFoundException(key);
      }
    }

    return value;
  }

  public Boolean getBooleanProperty(String key) {
    return Boolean.valueOf(getRequiredProperty(key));
  }

  public Optional<Integer> getPropertyAsInteger(String key) {
    String strValue = getProperty(key);

    if (StringUtils.isBlank(strValue)) {
      return Optional.empty();
    }

    try {
      Integer intValue = Integer.parseInt(strValue);
      return Optional.of(intValue);
    } catch (Exception e) {
      System.out.println();
      log.trace("Property value with key: {} cannot be parsed to integer", key);
    }

    return Optional.empty();
  }

  @Override
  public void run() {
    log.trace("Checking md5 at: {}", new Date());
    this.reloadConfigIfRequired();
    log.trace("Md5 verification completed at: " + new Date());
  }
}
