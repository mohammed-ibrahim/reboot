package org.reboot.server.secure.model;

public class InvalidHeaderException extends RuntimeException {

  public InvalidHeaderException(String message) {
    super(message);
  }
}
