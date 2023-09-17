package org.reboot.server.secure;

import org.reboot.server.secure.model.Packet;
import org.reboot.server.secure.reader.PacketReader;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ClientHelper {

  private static final String[] protocols = new String[]{"TLSv1.3"};
  private static final String[] cipher_suites = new String[]{"TLS_AES_128_GCM_SHA256"};

  private static final SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();

  public static void main(String[] args) {
    try {

      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("GET / HTTP/1.1"); stringBuilder.append(SockMain.CRLF);
      stringBuilder.append("Host: www.google.com"); stringBuilder.append(SockMain.CRLF);
      stringBuilder.append("User-Agent: curl/8.1.2"); stringBuilder.append(SockMain.CRLF);
      stringBuilder.append("Accept: */*"); stringBuilder.append(SockMain.CRLF);
      String s = makeAndReturnRequest("google.com", 443, stringBuilder.toString());
      System.out.println(s);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  public static String makeAndReturnRequest(String host, int port, String content) throws Exception {

    boolean testing = false;

    if (testing) {
      String s = "HTTP/1.1 200 OK\n" +
          "Date: Mon, 27 Jul 2009 12:28:53 GMT\n" +
          "Server: Apache/2.2.14 (Win32)\n" +
          "Last-Modified: Wed, 22 Jul 2009 19:15:56 GMT\n" +
          "Content-Length: 5\n" +
          "Content-Type: text/html\n" +
          "Connection: Closed\n" +
          "\n" +
          "12345";

      return s;
    }

    SSLSocket socket = null;
    PrintWriter out = null;
    BufferedReader in = null;

    try {
      socket = (SSLSocket) factory.createSocket(host, port);

//      socket.setEnabledProtocols(protocols);
//      socket.setEnabledCipherSuites(cipher_suites);

      socket.startHandshake();

      out = new PrintWriter(
          new BufferedWriter(
              new OutputStreamWriter(
                  socket.getOutputStream())));

      out.println(content);
      out.println("");
      out.flush();

      if (out.checkError()) {
        System.out.println("SSLSocketClient:  java.io.PrintWriter error");
      }

      /* read response */
//      in = new BufferedReader(
//          new InputStreamReader(
//              socket.getInputStream()));
//
//      String inputLine;
//      StringBuilder stringBuilder = new StringBuilder();
//      while ((inputLine = in.readLine()) != null) {
//        System.out.println(inputLine);
//        stringBuilder.append(inputLine);
//        stringBuilder.append(SockMain.CRLF);
//      }
//
//      System.out.println("READ COMPLETE");

      Packet packet = PacketReader.readPacket(socket, "asdf");
      return packet.getPacketString();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (socket != null)
        socket.close();
      if (out != null)
        out.close();
      if (in != null)
        in.close();
    }

    return null;
  }
}
