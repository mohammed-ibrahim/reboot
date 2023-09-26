package org.reboot.server.secure.core.stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.reboot.server.secure.model.StreamHandle;
import org.reboot.server.secure.util.IServerConfiguration;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Optional;

import static org.reboot.server.secure.core.stream.HttpStreamerImpl.CRLF;
import static org.testng.Assert.*;

public class HttpStreamerImplTest {

  public static String TEMPLATE = "HTTP/1.1 200 OK\r\nContent-Length: %s\r\n\r\n%s";

  @Test
  public void canStreamContentLength() throws Exception {
    String input = String.format(TEMPLATE, "5", "abcde");

    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(input.getBytes());
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    HttpStreamerImpl httpStreamer = new HttpStreamerImpl(getHeadProcessor());
    StreamHandle streamHandle = new StreamHandle(byteArrayInputStream, byteArrayOutputStream);
    httpStreamer.stream(streamHandle);

    String output = new String(byteArrayOutputStream.toByteArray());
    assertEquals(output, input);
  }

  @Test
  public void canStreamSimpleGetLength() throws Exception {
    String expected = "GET / HTTP/2" + CRLF +
        "Host: hello.com" + CRLF +
        "User-Agent: curl/8.1.2" + CRLF +
        "Accept: */*" + CRLF + CRLF;

    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(expected.getBytes());
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    HttpStreamerImpl httpStreamer = new HttpStreamerImpl(getHeadProcessor());
    StreamHandle streamHandle = new StreamHandle(byteArrayInputStream, byteArrayOutputStream);
    httpStreamer.stream(streamHandle);

    String actual = new String(byteArrayOutputStream.toByteArray());
    assertEquals(actual, expected);
  }

  @Test
  public void canStreamChunkedEncoding() throws Exception {

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    InputStream resourceAsStream = this.getClass().getResourceAsStream("/chunked-sample-1.txt");
    HttpStreamerImpl httpStreamer = new HttpStreamerImpl(getHeadProcessor());
    StreamHandle streamHandle = new StreamHandle(resourceAsStream, byteArrayOutputStream);
    httpStreamer.stream(streamHandle);

    String input = IOUtils.toString(this.getClass().getResourceAsStream("/chunked-sample-1.txt"));
    String output = new String(byteArrayOutputStream.toByteArray());
    assertEquals(output, input);
  }

  private IHeaderProcessor getHeadProcessor() {
    return new HeaderProcessorImpl(getServerConfig());
  }

  private IServerConfiguration getServerConfig() {
    return new IServerConfiguration() {
      @Override
      public Boolean getBooleanProperty(String key) {
        return false;
      }

      @Override
      public String getProperty(String key) {
        return "hello.com";
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