package org.reboot.server.secure.core.stream;

import org.reboot.server.secure.model.HeaderProcessingResponse;
import org.reboot.server.secure.model.HttpHeaderContext;
import org.reboot.server.secure.model.StreamContext;
import org.reboot.server.secure.model.StreamHandle;
import org.reboot.server.secure.util.IServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class HttpStreamerImpl implements IHttpStreamer {

  private static Logger log = LoggerFactory.getLogger(HttpStreamerImpl.class);


  public static final String DEST_SERVER_HOST = "dest.server.host";
  public static final String UPDATE_SERVER_HOST = "update.server.host.header";

  public static String CRLF = "\r\n";

  private IHeaderProcessor headerProcessor;

  private IServerConfiguration serverConfiguration;

  @Autowired
  public HttpStreamerImpl(IHeaderProcessor headerProcessor, IServerConfiguration serverConfiguration) {
    this.headerProcessor = headerProcessor;
    this.serverConfiguration = serverConfiguration;
  }

  @Override
  public void stream(StreamHandle streamHandle) throws Exception {

    String newHost = serverConfiguration.getProperty(DEST_SERVER_HOST);
    boolean updateHostHeader = serverConfiguration.getBooleanProperty(UPDATE_SERVER_HOST);
    StreamContext streamContext = new StreamContext(newHost, updateHostHeader);

    log.info("Reading headers, host modification allowed: {}", updateHostHeader);
    byte[] sessionBuffer = new byte[16*1024];
    HttpHeaderContext httpHeaderContext = streamHeaders(streamHandle, sessionBuffer, streamContext);
    log.info("Reading headers complete");

    if (httpHeaderContext.hasBody()) {
      log.info("Request has body");
      if (httpHeaderContext.isContentLength()) {
        streamBasedOnContentLength(httpHeaderContext, streamHandle, sessionBuffer);
      } else if (httpHeaderContext.isChunkedPacket()) {
        streamBasedOnChunkedData(streamHandle, sessionBuffer);
      } else {
        throw new RuntimeException("Unexpected");
      }
    }
  }

  private void streamBasedOnChunkedData(StreamHandle streamHandle, byte[] sessionBuffer) throws Exception  {

    byte[] lineBytes = readLineBytes(streamHandle.getInputStream(), sessionBuffer);
    while (lineBytes.length > 0) {
      String chunkSizeHexString = new String(lineBytes);
      int chunkSize = Integer.parseInt(chunkSizeHexString, 16);
      log.info("Chunk size: {}", chunkSize);

      addBytesToOutputAndTrace(streamHandle, lineBytes);
      addNewLineToOutputAndTrace(streamHandle);

      if (chunkSize == 0) {
//        this.outputStream.write(CRLF.getBytes());
        return;
      }

      this.streamBytes(chunkSize, streamHandle, sessionBuffer);
      addNewLineToOutputAndTrace(streamHandle);

      readLineBytes(streamHandle.getInputStream(), sessionBuffer);
      lineBytes = readLineBytes(streamHandle.getInputStream(), sessionBuffer);
    }
  }

  private void streamBasedOnContentLength(HttpHeaderContext httpHeaderContext, StreamHandle streamHandle, byte[] sessionBuffer) throws Exception {
    //clear the additional line after the headers.
    streamBytes(httpHeaderContext.getContentLength(), streamHandle, sessionBuffer);
  }

  public void streamBytes(int chunkSize, StreamHandle streamHandle, byte[] sessionBuffer) throws Exception {
    int numBytesRead = 0;

    while (numBytesRead < chunkSize) {
      int pendingBytes = calculatePendingBytes(numBytesRead, sessionBuffer.length, chunkSize);
      log.info("Reading from: {} to: {}", 0, pendingBytes);
      int read = streamHandle.getInputStream().read(sessionBuffer, 0, pendingBytes);
      log.info("Read complete, num bytes: {}", read);

      if (read == -1) {
        log.error("Unexpected break: {}", read);
        break;
      }

      if (read > 0) {
        numBytesRead += read;
        addBytesToOutputAndTraceWithStartAndEnd(streamHandle, sessionBuffer, 0, read);
      }
    }

    log.info("Successfully read chunk bytes");
  }

  private int calculatePendingBytes(int numBytesAlreadyRead, int bufferSize, int chunkSize) {
    if ((numBytesAlreadyRead + bufferSize) > chunkSize) {
      return chunkSize - numBytesAlreadyRead;
    }

    return bufferSize;
  }


  private HttpHeaderContext streamHeaders(StreamHandle streamHandle, byte[] sessionBuffer, StreamContext streamContext) throws Exception {
    HttpHeaderContext httpHeaderContext = new HttpHeaderContext();
    while (true) {
      byte[] data = readLineBytes(streamHandle.getInputStream(), sessionBuffer);

      log.debug("Read line: {}", new String(data));
      if (data.length < 1) {
        //as an empty line was read from input, empty line must be written to output.
        addNewLineToOutputAndTrace(streamHandle);
        break;
      }

      HeaderProcessingResponse headerProcessingResponse = this.headerProcessor.processHeader(data, httpHeaderContext, streamContext);
      writeToOutputAndTrace(streamHandle, data, headerProcessingResponse);
      addNewLineToOutputAndTrace(streamHandle);
    }

    return httpHeaderContext;
  }

  public void writeToOutputAndTrace(StreamHandle streamHandle,
                                    byte[] data,
                                    HeaderProcessingResponse headerProcessingResponse) throws Exception {

    if (headerProcessingResponse.isUpdateRequired()) {
      streamHandle.getOutputStream().write(headerProcessingResponse.getUpdatedContent());
    } else {
      streamHandle.getOutputStream().write(data);
    }
  }

  private void addNewLineToOutputAndTrace(StreamHandle streamHandle) throws Exception {
    streamHandle.getOutputStream().write(CRLF.getBytes());
  }

  private void addBytesToOutputAndTrace(StreamHandle streamHandle, byte[] data) throws Exception {
    streamHandle.getOutputStream().write(data);
  }

  private void addBytesToOutputAndTraceWithStartAndEnd(StreamHandle streamHandle,
                                                       byte[] sessionBuffer,
                                                       int start,
                                                       int end) throws Exception {
    streamHandle.getOutputStream().write(sessionBuffer, start, end);
  }

  public byte[] readLineBytes(InputStream inputStream, byte[] sessionBuffer) throws IOException {
    //TODO Add check if header greater than actual size.
    int index = 0;
    int readItem = inputStream.read();

    while (readItem != -1) {

      if (readItem == '\r') {
        int nextItem = inputStream.read();

        if (nextItem == '\n') {
          break;
        }
      } else {
        sessionBuffer[index] = (byte)readItem;
        index++;
        readItem = inputStream.read();
      }
    }

    byte[] fullBuffer = new byte[index];
    System.arraycopy(sessionBuffer, 0, fullBuffer, 0, index);
    return fullBuffer;
  }
}
