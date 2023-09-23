package org.reboot.server.secure.core;

import org.reboot.server.secure.util.Cfg;
import org.reboot.server.secure.util.IConfigurationProvider;

import java.util.Optional;

public class DefaultConfigurationProvider implements IConfigurationProvider {

  public DefaultConfigurationProvider() {
//    this.cfg = cfg;
  }

  @Override
  public String getProperty(String key) {
    return Cfg.getInstance().getProperty(key);
  }

  @Override
  public String getRequiredProperty(String key) {
    return Cfg.getInstance().getProperty(key);
  }

  @Override
  public Optional<Integer> getPropertyAsInteger(String key) {
    return Cfg.getInstance().getPropertyAsInteger(key);
  }
}
