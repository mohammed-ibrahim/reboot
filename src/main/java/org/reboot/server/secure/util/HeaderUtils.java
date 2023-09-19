package org.reboot.server.secure.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class HeaderUtils {
  public static String formatHeader(String header, int value) {
    return formatHeader(header, String.valueOf(value));
  }

  public static String formatHeader(String header, String value) {
    return String.format("%s: %s", header, value);
  }

  public static String getHeaderName(String line) {
    String [] parts = line.split("\\:");
    return StringUtils.toRootLowerCase(StringUtils.trim(parts[0]));
  }

  public static String getHeaderValue(String line) {
    String [] parts = line.split("\\:");
    return StringUtils.trim(parts[1]);
  }

  public static Optional<String> getHeaderFragment(String line, String key) {
    String headerValue = getHeaderValue(line);
    String lowerCase = headerValue.toLowerCase();
    String keyWithEqualsSign = StringUtils.toRootLowerCase(key + "=");
    if (!lowerCase.contains(keyWithEqualsSign)) {
      return Optional.empty();
    }

    String[] pairs = StringUtils.split(lowerCase, ";");

    for (String pair :pairs) {
      String trimmedKey = pair.trim();
      if (trimmedKey.startsWith(keyWithEqualsSign)) {
        return Optional.of(
            trimmedKey.replace(keyWithEqualsSign, "").trim()
        );
      }
    }

    return Optional.empty();
  }
}
