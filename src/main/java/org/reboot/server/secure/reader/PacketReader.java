package org.reboot.server.secure.reader;

import org.apache.commons.lang3.StringUtils;
import org.reboot.server.secure.SockMain;
import org.reboot.server.secure.model.Packet;
import org.reboot.server.secure.util.HeaderUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PacketReader {
  public static final String CONTENT_LENGTH = "content-length";

  public static final String TRANSFER_ENCODING = "transfer-encoding";
  public static Packet readPacket(InputStream inputStream) throws Exception {
    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

    List<String> buffer = new ArrayList<>();
    String line;
    int contentLength = 0;
    boolean transferEncoding = false;
    while (true) {
//      line = readWithTimeout(in);
      line = in.readLine();

      System.out.println("Line read: " + line);
      if (StringUtils.isBlank(line)) {
        break;
      }

      if (line.toLowerCase().startsWith(CONTENT_LENGTH)) {
        contentLength = parseContentLength(line);
      }

      if (line.toLowerCase().startsWith(TRANSFER_ENCODING)) {
        transferEncoding = true;
      }

//      if (line.toLowerCase().startsWith("host")) {
//        buffer.add("host: " + newHost);
//      } else {
//        buffer.add(line);
//      }

      buffer.add(line);
    }

    Packet packet = new Packet();
    packet.setHead(buffer);
    packet.setBody(null);


    if (contentLength > 0) {
      readBasedOnContentLength(in, contentLength, packet);
      return packet;
    }

    if (transferEncoding) {
      //TODO: Ensure to fail if header value is other than chunked.
      readBasedOnTransferEncoding(in, packet);
    }

    return packet;
  }

  private static void readBasedOnTransferEncoding(BufferedReader in, Packet packet) throws Exception {
    String body = ChunkedBodyReader.read(in);
    packet.setBody(body);
    List<String> updatedHead = packet
        .getHead()
        .stream()
        .filter(header -> !header.toLowerCase().startsWith(TRANSFER_ENCODING))
        .collect(Collectors.toList());
    packet.setHead(new ArrayList<>(updatedHead));
    packet.getHead().add(HeaderUtils.formatHeader(CONTENT_LENGTH, body.length()));
  }

  private static void readBasedOnContentLength(BufferedReader in, int contentLength, Packet packet) throws Exception {
    packet.setBody(ChunkedBodyReader.readBytes(in, contentLength, contentLength));
  }

  private static int parseContentLength(String line) {
    String [] parts = line.split("\\:");
    return Integer.parseInt(parts[1].trim());
  }

  private static String readWithTimeout(BufferedReader in) {
    CompletableFuture<String> stringCompletableFuture = CompletableFuture.supplyAsync(() -> {
      try {
        return in.readLine();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    try {
      return stringCompletableFuture.get(5000, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
