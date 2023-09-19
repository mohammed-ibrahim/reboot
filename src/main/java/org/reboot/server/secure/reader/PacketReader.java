package org.reboot.server.secure.reader;

import org.reboot.server.secure.model.Packet;
import org.reboot.server.secure.util.HeaderUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PacketReader {

  public static Packet readPacket(InputStream inputStream) throws Exception {
    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
//    PacketHead packetHead = HeadReader.readHead(inputStream);
    PacketHead packetHead = HeadReader.readWithExistingBuffer(in);

    Packet packet = new Packet();
    packet.setHead(packetHead.getHeadLines());
    packet.setBody(null);

    int contentLength = packetHead.getContentLength();
    if (contentLength > 0) {
      readBasedOnContentLength(in, contentLength, packet);
      return packet;
    } else if (packetHead.isChunked()) {
      //TODO: Ensure to fail if header value is other than chunked.
      readBasedOnTransferEncoding(in, inputStream, packet, packetHead.getEncoding());
    }

    return packet;
  }

  private static void readBasedOnTransferEncoding(BufferedReader in, InputStream inputStream, Packet packet, String encoding) throws Exception {
//    String body = LineBasedReader.read(inputStream, encoding);
    String body = LineBasedReader.readWithExistingBuffer(in);
//    String body = ChunkedBodyReader.read(in);
    packet.setBody(body);
//    List<String> updatedHead = packet
//        .getHead()
//        .stream()
//        .filter(header -> !header.toLowerCase().startsWith(HeadReader.TRANSFER_ENCODING))
//        .collect(Collectors.toList());
//    packet.setHead(new ArrayList<>(updatedHead));
//    packet.getHead().add(HeaderUtils.formatHeader(HeadReader.CONTENT_LENGTH, body.length()));
  }

  private static void readBasedOnContentLength(BufferedReader in, int contentLength, Packet packet) throws Exception {
    packet.setBody(ChunkedBodyReader.readBytes(in, contentLength, contentLength));
  }
}
