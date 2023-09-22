package org.reboot.server.secure.util;

import java.util.Optional;

public interface IConfigurationProvider {

  String getProperty(String key);

  String getRequiredProperty(String key);

  Optional<Integer> getPropertyAsInteger(String key);
}
