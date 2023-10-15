package org.reboot.server.secure.core.stream;

import org.apache.commons.lang3.StringUtils;
import org.reboot.server.secure.model.HeaderProcessingResponse;
import org.reboot.server.secure.model.HttpHeaderContext;
import org.reboot.server.secure.model.HttpVersion;
import org.reboot.server.secure.model.RequestContext;
import org.reboot.server.secure.model.StreamHandle;
import org.reboot.server.secure.model.StreamResponse;
import org.reboot.server.secure.util.IServerConfiguration;
import org.reboot.server.secure.util.IStreamTrace;
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

  private IStreamTrace streamTrace;

  @Autowired
  public HttpStreamerImpl(IHeaderProcessor headerProcessor, IServerConfiguration serverConfiguration, IStreamTrace streamTrace) {
    this.headerProcessor = headerProcessor;
    this.serverConfiguration = serverConfiguration;
    this.streamTrace = streamTrace;
  }

  @Override
  public StreamResponse stream(RequestContext requestContext, StreamHandle streamHandle, boolean forwardStream) throws Exception {
    streamTrace.start(streamHandle.getTraceContext());

    log.info("Reading headers, host modification allowed: {}", requestContext.isUpdateHostHeader());
    byte[] sessionBuffer = new byte[16*1024];
    HttpHeaderContext httpHeaderContext = streamHeaders(streamHandle, sessionBuffer, requestContext, forwardStream);
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
    streamTrace.end(streamHandle.getTraceContext());
  }

  private void streamBasedOnChunkedData(StreamHandle streamHandle, byte[] sessionBuffer) throws Exception  {

    //TODO: Truncate the extension data for chunk-size header.
    int numBytesRead = readLineToSessionBuffer(streamHandle.getInputStream(), sessionBuffer);
    while (numBytesRead > 0) {
      //At this place chunkSize is already read in sessionBuffer.
      String chunkSizeHexString = new String(sessionBuffer, 0, numBytesRead);
      int chunkSize = Integer.parseInt(chunkSizeHexString, 16);
      log.info("Chunk size: {}", chunkSize);

      //Write the chunkSize to sessionBuffer
      addBytesToOutputAndTraceWithStartAndEnd(streamHandle, sessionBuffer, 0, numBytesRead);
      addNewLineToOutputAndTrace(streamHandle);

      if (chunkSize == 0) {
        /*
        There will be two new lines after last chunk (chunkSize = 0)
        ref1: https://datatracker.ietf.org/doc/html/rfc9112#section-7.1
        ref2: https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Transfer-Encoding

        line immediately after 0 chunkSize is already read and written before this if block.
        Next line is read and written after the below comment.
         */
        readLineToSessionBuffer(streamHandle.getInputStream(), sessionBuffer);
        addNewLineToOutputAndTrace(streamHandle);
        return;
      }

      this.streamBytes(chunkSize, streamHandle, sessionBuffer);
      addNewLineToOutputAndTrace(streamHandle);

      // Read the additional CLRF after chunk-body
      readLineToSessionBuffer(streamHandle.getInputStream(), sessionBuffer);
      numBytesRead = readLineToSessionBuffer(streamHandle.getInputStream(), sessionBuffer);
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


  private HttpHeaderContext streamHeaders(StreamHandle streamHandle, byte[] sessionBuffer, RequestContext requestContext, boolean forwardStream) throws Exception {
    HttpHeaderContext httpHeaderContext = new HttpHeaderContext();

    while (true) {
//      byte[] data = readLineBytes(streamHandle.getInputStream(), sessionBuffer);
      int numBytesRead = readLineToSessionBuffer(streamHandle.getInputStream(), sessionBuffer);

      if (forwardStream && requestContext.getHttpVersion() == null) {
        setHttpVersion(sessionBuffer, numBytesRead, requestContext);
      }

      log.debug("Read line: {}", new String(sessionBuffer, 0, numBytesRead));
      if (numBytesRead < 1) {
        //as an empty line was read from input, empty line must be written to output.
        addNewLineToOutputAndTrace(streamHandle);
        break;
      }

      HeaderProcessingResponse headerProcessingResponse = this.headerProcessor.processHeader(sessionBuffer, numBytesRead, httpHeaderContext, requestContext);
      writeToOutputAndTrace(streamHandle, sessionBuffer, numBytesRead, headerProcessingResponse);
      addNewLineToOutputAndTrace(streamHandle);
    }

    return httpHeaderContext;
  }

  private void setHttpVersion(byte[] sessionBuffer, int numBytesRead, RequestContext requestContext) {
    String line = new String(sessionBuffer, 0, numBytesRead);
    String parts[] = line.split(" ");
    String lastPart = parts[parts.length-1];

    if (StringUtils.equalsIgnoreCase(lastPart, "HTTP/1.1")) {
      requestContext.setHttpVersion(HttpVersion.HTTP_1_1);
    } else if (StringUtils.equalsIgnoreCase(lastPart, "HTTP/2")) {
      requestContext.setHttpVersion(HttpVersion.HTTP_2);
    }
  }

  private void setRequestLineStatusDetails(RequestContext requestContext) {
  }

  public void writeToOutputAndTrace(StreamHandle streamHandle,
                                    byte[] sessionBuffer,
                                    int limit,
                                    HeaderProcessingResponse headerProcessingResponse) throws Exception {

    if (headerProcessingResponse.isUpdateRequired()) {
      streamHandle.getOutputStream().write(headerProcessingResponse.getUpdatedContent());
    } else {
      streamHandle.getOutputStream().write(sessionBuffer, 0, limit);
    }

    streamTrace.addModifiedTrace(streamHandle.getTraceContext(), sessionBuffer, 0, limit, headerProcessingResponse.isUpdateRequired(), headerProcessingResponse.getUpdatedContent());
  }

  private void addNewLineToOutputAndTrace(StreamHandle streamHandle) throws Exception {
    streamHandle.getOutputStream().write(CRLF.getBytes());
    streamTrace.addTrace(streamHandle.getTraceContext(), CRLF.getBytes());
  }

  private void addBytesToOutputAndTraceWithStartAndEnd(StreamHandle streamHandle,
                                                       byte[] sessionBuffer,
                                                       int start,
                                                       int end) throws Exception {
    streamHandle.getOutputStream().write(sessionBuffer, start, end);
    streamTrace.addTrace(streamHandle.getTraceContext(), sessionBuffer, start, end);
  }

  public int readLineToSessionBuffer(InputStream inputStream, byte[] sessionBuffer) throws IOException {
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

    return index;
  }
}
