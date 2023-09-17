package org.reboot.server.secure;

import org.reboot.server.secure.model.Packet;
import org.reboot.server.secure.reader.PacketReader;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SockMain {

  public static String CRLF = "\r\n";
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

  private static void processRequestAndSendResponse(Socket socket) throws Exception {
    Packet packet = PacketReader.readPacket(socket, destServer);
    System.out.println("READ COMPLETE FROM CLIENT:");

    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

    String respFromServer = ClientHelper.makeAndReturnRequest(destServer, 443, packet.getPacketString());
    System.out.println("Forwarding: " + packet.getPacketString());
    System.out.println("Response: " + respFromServer);
    out.write(respFromServer);
    out.flush();
    out.close();
    socket.close();
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
}
