package org.reboot.server.secure;

import org.apache.commons.lang3.StringUtils;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SockMain {

  private static String serverFilePath = null;
  private static String serverFilePassword = null;

  private static String destServer = null;

  public static void main(String[] args) throws Exception {

    serverFilePath = System.getProperty("server.certificate");
    serverFilePassword = System.getProperty("server.certificate.password");
    destServer = System.getProperty("dest.server.host");

    SSLServerSocketFactory sslSocketFactory = getSSLSocketFactory();
    SSLServerSocket sslServerSocket = (SSLServerSocket)sslSocketFactory.createServerSocket(8081);
    ExecutorService executorService = Executors.newFixedThreadPool(10);

    while (true) {
      Socket socket = sslServerSocket.accept();

      executorService.submit(() -> {
        safeProcessRequestAndSendResponse(socket);
      });
    }
  }

  private static void safeProcessRequestAndSendResponse(Socket socket) {
    try {
      processRequestAndSendResponse(socket);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void processRequestAndSendResponse(Socket socket) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

    byte[] bytes = readerToBytes(in);
    String content = new String(bytes);

//    String R200 = "HTTP/1.1 200 OK\r\nContent-Length: %s\r\n\r\n%s";

    String respFromServer = useSslSocket(bytes);
    System.out.println("Forwarding: " + content);
    System.out.println("Response: " + respFromServer);
    out.write(respFromServer);
    out.flush();
    out.close();
    socket.close();
  }

  private static String useSslSocket(byte[] toSend) throws IOException {
    final SocketFactory socketFactory = SSLSocketFactory.getDefault();
    try (final Socket socket = socketFactory.createSocket(destServer, 443)) {
      final OutputStream outputStream = socket.getOutputStream();
      outputStream.write(toSend);

      final InputStream inputStream = socket.getInputStream();
      final String response = readAsString(inputStream);

      return response;
    }
  }

  private static String readAsString(final InputStream inputStream) throws IOException {
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    final byte[] buffer = new byte[8 * 1024];
    int bytesRead;
    while ((bytesRead = inputStream.read(buffer)) > 0) {
      outputStream.write(buffer, 0, bytesRead);
    }
    return outputStream.toString(StandardCharsets.UTF_8.name());
  }


  private static SSLServerSocketFactory getSSLSocketFactory() throws Exception {
    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    keyStore.load(new FileInputStream(new File(serverFilePath)), serverFilePassword.toCharArray());
    KeyManagerFactory keyMan = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyMan.init(keyStore, serverFilePassword.toCharArray());
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(keyMan.getKeyManagers(), null, null);
    SSLServerSocketFactory sslFactory = sslContext.getServerSocketFactory();
    return sslFactory;
  }

  public static byte[] readerToBytes(BufferedReader in) throws IOException {
//    char[] charArray = new char[8 * 1024];
    StringBuilder builder = new StringBuilder();
//    int numCharsRead;
    System.out.println("Waiting to read from socket");

    String line;
    while (true) {
      line = in.readLine();
      if (StringUtils.isBlank(line)) {
        break;
      }

      builder.append(line);
    }

    byte[] targetArray = builder.toString().getBytes();
    return targetArray;
  }
}
