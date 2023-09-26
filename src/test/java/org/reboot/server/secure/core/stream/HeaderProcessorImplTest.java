package org.reboot.server.secure.core.stream;

import org.reboot.server.secure.model.HeaderProcessingResponse;
import org.reboot.server.secure.model.HttpHeaderContext;
import org.reboot.server.secure.model.StreamContext;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;

import static org.testng.Assert.*;

public class HeaderProcessorImplTest {


  private static String CONTENT_LENGTH = "Content-length: 80";
  private static String TRANSFER_ENCODING = "transfer-encoding: chunked";
  private static String NEW_HOST = "newhost";
  private static String OLD_HOST_HEADER = "hoST: oldhost";
  private static String NEW_HOST_HEADER = "host: " + NEW_HOST;


  @Test
  public void canDetectContentLength() throws Exception {
    HeaderProcessorImpl headerProcessor = new HeaderProcessorImpl();
    HttpHeaderContext httpHeaderContext = new HttpHeaderContext();
    headerProcessor.processHeader(CONTENT_LENGTH.getBytes(), httpHeaderContext, getStreamContext(NEW_HOST, false));

    assertTrue(httpHeaderContext.hasBody());
    assertEquals(httpHeaderContext.getContentLength(), 80);
    assertFalse(httpHeaderContext.isChunkedPacket());
  }

  @Test
  public void canDetectTransferEncoding() throws Exception {
    HttpHeaderContext httpHeaderContext = new HttpHeaderContext();
    HeaderProcessorImpl headerProcessor = new HeaderProcessorImpl();

    headerProcessor.processHeader(TRANSFER_ENCODING.getBytes(), httpHeaderContext, getStreamContext(NEW_HOST, false));

    assertTrue(httpHeaderContext.hasBody());
    assertFalse(httpHeaderContext.isContentLength());
    assertTrue(httpHeaderContext.isChunkedPacket());
  }

  @Test
  public void canReplaceNewHost() throws Exception {
    HeaderProcessorImpl headerProcessor = new HeaderProcessorImpl();
    HttpHeaderContext httpHeaderContext = new HttpHeaderContext();
    HeaderProcessingResponse headerProcessingResponse = headerProcessor.processHeader(OLD_HOST_HEADER.getBytes(), httpHeaderContext, getStreamContext(NEW_HOST, true));

    assertFalse(httpHeaderContext.hasBody());
    assertTrue(headerProcessingResponse.isUpdateRequired());
    assertEquals(NEW_HOST_HEADER, new String(headerProcessingResponse.getUpdatedContent()));
  }

  private StreamContext getStreamContext(String host, boolean updateHostHeader) {

    return new StreamContext(host, updateHostHeader);
//    return new IServerConfiguration() {
//      @Override
//      public Boolean getBooleanProperty(String key) {
//        return updateHostHeader;
//      }
//
//      @Override
//      public String getProperty(String key) {
//        return host;
//      }
//
//      @Override
//      public String getRequiredProperty(String key) {
//        return null;
//      }
//
//      @Override
//      public String getProperty(String key, boolean failIfNotConfigured) {
//        return null;
//      }
//
//      @Override
//      public Optional<Integer> getPropertyAsInteger(String key) {
//        return Optional.empty();
//      }
//    };
  }

}