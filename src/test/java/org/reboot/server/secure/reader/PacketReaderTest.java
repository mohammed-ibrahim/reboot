package org.reboot.server.secure.reader;

import org.reboot.server.secure.model.Packet;
import org.testng.annotations.Test;

import java.io.InputStream;

import static org.testng.Assert.*;
public class PacketReaderTest {

  @Test
  public void canPacketReaderReadChunkedPacket() throws Exception {
    InputStream resourceAsStream = this.getClass().getResourceAsStream("/chunked-sample-1.txt");
    Packet packet = PacketReader.readPacket(resourceAsStream);
    assertEquals("abcdefghijklmnopqrst", packet.getBody());
    assertEquals("HTTP/1.1 200 OK", packet.getHead().get(0));
  }

  @Test
  public void canPacketReaderReadContentLengthPacket() throws Exception {
    InputStream resourceAsStream = this.getClass().getResourceAsStream("/content-length-sample-1.txt");
    Packet packet = PacketReader.readPacket(resourceAsStream);
    assertEquals("abcde", packet.getBody());
    assertEquals("HTTP/1.1 200 OK", packet.getHead().get(0));
  }
}