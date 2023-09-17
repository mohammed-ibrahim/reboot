package org.reboot.server.secure.util;

import org.reboot.server.secure.model.Packet;

public class PacketUtils {

  public static final String HOST_HEADER = "host";
  public static final String HOST_HEADER_WITH_COLON = HOST_HEADER + ":";

  public static void updateHostHeader(Packet packet, String newHost) {
    int index = -1;
    for (int i=0; i<packet.getHead().size(); i++) {
      String header = packet.getHead().get(i);

      if (header.toLowerCase().startsWith(HOST_HEADER_WITH_COLON)) {
        index = i;
        break;
      }
    }

    if (index == -1) {
      throw new RuntimeException("host header not found");
    }

    packet.getHead().set(index, HeaderUtils.formatHeader(HOST_HEADER, newHost));
  }

  public static String getPathFromRequestPacket(Packet packet) {
    String statusLine = packet.getHead().get(0);
    String parts[] = statusLine.split(" ");
    String uri = parts[1];
    String path = uri.split("\\?")[0];
    return path;
  }
}
