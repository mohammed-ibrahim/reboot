package org.reboot.server.secure.model;

import org.apache.commons.lang3.RandomStringUtils;

public class RequestContext {

  private String id;

  private String destinationHostName;

  private boolean updateHostHeader;

  public RequestContext() {
    this.id = RandomStringUtils.randomAlphabetic(10);
  }

  public String getId() {
    return id;
  }

  public String getDestinationHostName() {
    return destinationHostName;
  }

  public void setDestinationHostName(String destinationHostName) {
    this.destinationHostName = destinationHostName;
  }

  public boolean isUpdateHostHeader() {
    return updateHostHeader;
  }

  public void setUpdateHostHeader(boolean updateHostHeader) {
    this.updateHostHeader = updateHostHeader;
  }
}
