package org.reboot.server.secure.util;

import java.util.Optional;

public interface IServerConfiguration {

  Boolean getBooleanProperty(String key);

  String getProperty(String key);

  String getRequiredProperty(String key);

  String getProperty(String key, boolean failIfNotConfigured);

  Optional<Integer> getPropertyAsInteger(String key);
}
