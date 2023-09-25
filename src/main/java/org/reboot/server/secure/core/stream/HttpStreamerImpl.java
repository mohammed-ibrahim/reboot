package org.reboot.server.secure.core.stream;

import org.reboot.server.secure.model.HttpHeaderContext;
import org.reboot.server.secure.model.StreamHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class HttpStreamerImpl implements IHttpStreamer {

  private static Logger log = LoggerFactory.getLogger(HttpStreamerImpl.class);
  public static String CRLF = "\r\n";

  private IHeaderProcessor headerProcessor;

  @Autowired
  public HttpStreamerImpl(IHeaderProcessor headerProcessor) {
    this.headerProcessor = headerProcessor;
  }

  @Override
  public void stream(StreamHandle streamHandle) throws Exception {
    log.info("Reading headers");
    byte[] sessionBuffer = new byte[16*1024];
    HttpHeaderContext httpHeaderContext = streamHeaders(streamHandle, sessionBuffer);
    log.info("Reading headers complete");

    if (httpHeaderContext.hasBody()) {
      log.info("Request has body");
      streamHandle.getOutputStream().write(CRLF.getBytes());
      if (httpHeaderContext.isContentLength()) {
        streamBasedOnContentLength(httpHeaderContext, streamHandle, sessionBuffer);
      } else if (httpHeaderContext.isChunkedPacket()) {
        streamBasedOnChunkedData(streamHandle, sessionBuffer);
      } else {
        throw new RuntimeException("Unexpected");
      }
    } else {
      streamHandle.getOutputStream().write(CRLF.getBytes());
    }
  }

  private void streamBasedOnChunkedData(StreamHandle streamHandle, byte[] sessionBuffer) throws Exception  {

    byte[] lineBytes = readLineBytes(streamHandle.getInputStream(), sessionBuffer);
    while (lineBytes.length > 0) {
      String chunkSizeHexString = new String(lineBytes);
      int chunkSize = Integer.parseInt(chunkSizeHexString, 16);
      log.info("Chunk size: {}", chunkSize);

      streamHandle.getOutputStream().write(lineBytes);
      streamHandle.getOutputStream().write(CRLF.getBytes());

      if (chunkSize == 0) {
//        this.outputStream.write(CRLF.getBytes());
        return;
      }

      this.streamBytes(chunkSize, streamHandle, sessionBuffer);
      streamHandle.getOutputStream().write(CRLF.getBytes());

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
        streamHandle.getOutputStream().write(sessionBuffer, 0, read);
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


  private HttpHeaderContext streamHeaders(StreamHandle streamHandle, byte[] sessionBuffer) throws Exception {
    HttpHeaderContext httpHeaderContext = new HttpHeaderContext();
    while (true) {
      byte[] data = readLineBytes(streamHandle.getInputStream(), sessionBuffer);

      log.debug("Read line: {}", new String(data));
      if (data.length < 1) {
        break;
      }

      this.headerProcessor.writeHeader(data, streamHandle.getOutputStream(), httpHeaderContext);
      streamHandle.getOutputStream().write(CRLF.getBytes());
    }

    return httpHeaderContext;
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
