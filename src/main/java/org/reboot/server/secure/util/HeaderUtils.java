package org.reboot.server.secure.util;

public class HeaderUtils {
  public static String formatHeader(String header, int value) {
    return formatHeader(header, String.valueOf(value));
  }

  public static String formatHeader(String header, String value) {
    return String.format("%s: %s", header, value);
  }
}
