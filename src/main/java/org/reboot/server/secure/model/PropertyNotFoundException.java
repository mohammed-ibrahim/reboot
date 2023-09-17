package org.reboot.server.secure.model;

public class PropertyNotFoundException extends RuntimeException {
  private String key;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public PropertyNotFoundException(String key) {
    super("Property not configured: " + key);
  }
}
