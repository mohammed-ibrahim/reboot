package org.reboot.server.secure.model;

public enum FrameType {

  UNKNOWN,
  DATA,
  HEADERS,
  PRIORITY,
  RST_STREAM,
  SETTINGS,
  PUSH_PROMISE,
  PING,
  GOAWAY,
  WINDOW_UPDATE,
  CONTINUATION,
  ALTSVC,
  ORIGIN;
}
