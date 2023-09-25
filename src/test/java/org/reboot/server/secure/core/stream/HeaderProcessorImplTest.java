package org.reboot.server.secure.core.stream;

import org.reboot.server.secure.model.HttpHeaderContext;
import org.reboot.server.secure.util.IServerConfiguration;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.util.Optional;

import static org.testng.Assert.*;

public class HeaderProcessorImplTest {


  private static String CONTENT_LENGTH = "Content-length: 80";
  private static String TRANSFER_ENCODING = "transfer-encoding: chunked";
  private static String NEW_HOST = "newhost";
  private static String OLD_HOST_HEADER = "hoST: oldhost";
  private static String NEW_HOST_HEADER = "host: " + NEW_HOST;


  @Test
  public void canDetectContentLength() throws Exception {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    HeaderProcessorImpl headerProcessor = new HeaderProcessorImpl(getServerConfig(NEW_HOST, false));
    HttpHeaderContext httpHeaderContext = new HttpHeaderContext();
    headerProcessor.writeHeader(CONTENT_LENGTH.getBytes(), byteArrayOutputStream, httpHeaderContext);

    assertTrue(httpHeaderContext.hasBody());
    assertEquals(httpHeaderContext.getContentLength(), 80);
    assertFalse(httpHeaderContext.isChunkedPacket());
    String processed = new String(byteArrayOutputStream.toByteArray());
    assertEquals(processed, CONTENT_LENGTH);
  }

  @Test
  public void canDetectTransferEncoding() throws Exception {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    HttpHeaderContext httpHeaderContext = new HttpHeaderContext();
    HeaderProcessorImpl headerProcessor = new HeaderProcessorImpl(getServerConfig(NEW_HOST, false));

    headerProcessor.writeHeader(TRANSFER_ENCODING.getBytes(), byteArrayOutputStream, httpHeaderContext);

    assertTrue(httpHeaderContext.hasBody());
    assertFalse(httpHeaderContext.isContentLength());
    assertTrue(httpHeaderContext.isChunkedPacket());
    String processed = new String(byteArrayOutputStream.toByteArray());
    assertEquals(processed, TRANSFER_ENCODING);
  }

  @Test
  public void canReplaceNewHost() throws Exception {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    HeaderProcessorImpl headerProcessor = new HeaderProcessorImpl(getServerConfig(NEW_HOST, true));
    HttpHeaderContext httpHeaderContext = new HttpHeaderContext();
    headerProcessor.writeHeader(OLD_HOST_HEADER.getBytes(), byteArrayOutputStream, httpHeaderContext);

    assertFalse(httpHeaderContext.hasBody());
    String processed = new String(byteArrayOutputStream.toByteArray());
    assertEquals(processed, NEW_HOST_HEADER);
  }

  private IServerConfiguration getServerConfig(String host, boolean updateHostHeader) {
    return new IServerConfiguration() {
      @Override
      public Boolean getBooleanProperty(String key) {
        return updateHostHeader;
      }

      @Override
      public String getProperty(String key) {
        return host;
      }

      @Override
      public String getRequiredProperty(String key) {
        return null;
      }

      @Override
      public String getProperty(String key, boolean failIfNotConfigured) {
        return null;
      }

      @Override
      public Optional<Integer> getPropertyAsInteger(String key) {
        return Optional.empty();
      }
    };
  }

}