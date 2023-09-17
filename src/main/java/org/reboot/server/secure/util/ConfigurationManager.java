package org.reboot.server.secure.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.reboot.server.secure.model.PropertyNotFoundException;

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

public class ConfigurationManager implements Runnable {
  private ScheduledExecutorService executorService;
  private String previouslyKnownMd5;

  private String configFilePath;

  private Map<String, String> properties;

  private static ConfigurationManager instance;

  public static void setup(String filename) {
    instance = new ConfigurationManager(filename);
  }

  public static ConfigurationManager getInstance() {
    if (instance == null) {
      throw new RuntimeException("NOT SETUP");
    }
    return instance;
  }

  private ConfigurationManager(String filename) {

    File file = new File(filename);
    if (!file.exists()) {
      throw new RuntimeException("APP CONFIG FILE NOT EXISTS: " + filename);
    }

    this.configFilePath = filename;
    this.properties = new ConcurrentHashMap<>();
    this.executorService = Executors.newSingleThreadScheduledExecutor();
    this.forceLoadConfigurationAndUpdateFileMd5();
    System.out.println(String.format("Scheduling md5 verifier at: " + new Date()));
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
    System.out.println(String.format("Previously-Known md5: %s recentlyCalculatedMd5 md5: %s", this.previouslyKnownMd5, recentlyCalculatedMd5));

    if (!StringUtils.equalsIgnoreCase(this.previouslyKnownMd5, recentlyCalculatedMd5)) {
      System.out.println(String.format("Reloading configuration"));
      forceLoadConfigurationAndUpdateFileMd5();
      System.out.println(String.format("Reloading configuration complete"));
    } else {
      System.out.println(String.format("Reloading configuration not required."));
    }
  }

  private String getMd5HexOfConfigFile() {
    try (InputStream is = Files.newInputStream(Paths.get(this.configFilePath))) {
      String md5 = DigestUtils.md5Hex(is);
      return md5;
    } catch (Exception e) {
      System.out.println("Failed to calculate md5");
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
      System.out.println("Property value not found for key: " + key + " using empty value");
      if (failIfNotConfigured) {
        throw new PropertyNotFoundException(key);
      }
    }

    return value;
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
      System.out.println("Property value with key: " + key + " cannot be parsed to integer");
    }

    return Optional.empty();
  }

  @Override
  public void run() {
    System.out.println(String.format("Checking md5 at: " + new Date()));
    this.reloadConfigIfRequired();
    System.out.println(String.format("Md5 verification completed at: " + new Date()));
  }
}
