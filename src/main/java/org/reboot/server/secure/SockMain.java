package org.reboot.server.secure;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.KeyStore;

public class SockMain {

  private static String serverFilePath = null;
  private static String serverFilePassword = null;

  public static void main(String[] args) throws Exception {

    serverFilePath = System.getProperty("server.certificate");
    serverFilePassword = System.getProperty("server.certificate.password");

    SSLServerSocketFactory sslSocketFactory = getSSLSocketFactory();
    SSLServerSocket sslServerSocket = (SSLServerSocket)sslSocketFactory.createServerSocket(8081);

    Socket socket = sslServerSocket.accept();

    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

    byte[] bytes = readerToBytes(in);
    String content = new String(bytes);
    System.out.println(content);
    String R200 = "HTTP/1.1 200 OK\r\nContent-Length: %s\r\n\r\n%s";
    out.write(String.format(R200, "hello".length(), "hello"));
    out.flush();
    out.close();
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
    char[] charArray = new char[8 * 1024];
    StringBuilder builder = new StringBuilder();
    int numCharsRead;
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
