package org.reboot.server.secure.model;

public class HeaderProcessingResponse {

  boolean updateRequired;

  byte[] updatedContent;

  public HeaderProcessingResponse(boolean updateRequired, byte[] updatedContent) {
    this.updateRequired = updateRequired;
    this.updatedContent = updatedContent;
  }

  public boolean isUpdateRequired() {
    return updateRequired;
  }

  public byte[] getUpdatedContent() {
    return updatedContent;
  }
}
