package org.reboot.server.secure.model;

import org.apache.commons.lang3.RandomStringUtils;

public class RequestContext {

  private String id;

  private String destinationHostName;

  private boolean updateHostHeader;

  private HttpVersion httpVersion;

  public RequestContext() {
    this.id = RandomStringUtils.randomAlphabetic(10);
    httpVersion = null;
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

  public HttpVersion getHttpVersion() {
    return httpVersion;
  }

  public void setHttpVersion(HttpVersion httpVersion) {
    this.httpVersion = httpVersion;
  }
}
