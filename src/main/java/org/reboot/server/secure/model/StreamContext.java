package org.reboot.server.secure.model;

public class StreamContext {

  private String newHostName;

  private boolean updateHostHeader;

  public StreamContext(String newHostName, boolean updateHostHeader) {
    this.newHostName = newHostName;
    this.updateHostHeader = updateHostHeader;
  }

  public String getNewHostName() {
    return newHostName;
  }

  public boolean isUpdateHostHeader() {
    return updateHostHeader;
  }
}
