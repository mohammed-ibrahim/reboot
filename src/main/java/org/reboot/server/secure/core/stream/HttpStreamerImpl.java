package org.reboot.server.secure.core.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HttpStreamerImpl implements IHttpStreamer {

  private static Logger log = LoggerFactory.getLogger(HttpStreamerImpl.class);
  public static String CRLF = "\r\n";
  private InputStream inputStream;

  private OutputStream outputStream;

  private IHeaderProcessor headerProcessor;

  private byte[] instanceBuffer;

  private int instanceBufferSize;

  public HttpStreamerImpl(InputStream inputStream, OutputStream outputStream, IHeaderProcessor headerProcessor) {
    this.inputStream = inputStream;
    this.outputStream = outputStream;
    this.headerProcessor = headerProcessor;
    this.instanceBufferSize = 16*1024;
    this.instanceBuffer = new byte[this.instanceBufferSize];
  }

  @Override
  public void stream() throws Exception {
    log.info("Reading headers");
    streamHeaders();
    log.info("Reading headers complete");

    if (headerProcessor.hasBody()) {
      log.info("Request has body");
      this.outputStream.write(CRLF.getBytes());
      if (headerProcessor.isContentLength()) {
        streamBasedOnContentLength();
      } else if (headerProcessor.isChunkedPacket()) {
        streamBasedOnChunkedData();
      } else {
        throw new RuntimeException("Unexpected");
      }
    }

    this.outputStream.close();
    this.instanceBuffer.clone();
  }

  private void streamBasedOnChunkedData() throws Exception  {

    byte[] lineBytes = readLineBytes();
    while (lineBytes.length > 0) {
      String chunkSizeHexString = new String(lineBytes);
      int chunkSize = Integer.parseInt(chunkSizeHexString, 16);
      log.info("Chunk size: {}", chunkSize);

      this.outputStream.write(lineBytes);
      this.outputStream.write(CRLF.getBytes());

      if (chunkSize == 0) {
//        this.outputStream.write(CRLF.getBytes());
        return;
      }

      this.streamBytes(chunkSize);
      this.outputStream.write(CRLF.getBytes());

      readLineBytes();
      lineBytes = readLineBytes();
    }
  }

  private void streamBasedOnContentLength() throws Exception {
    //clear the additional line after the headers.
    streamBytes(this.headerProcessor.getContentLength());
  }

  public void streamBytes(int chunkSize) throws Exception {
    int numBytesRead = 0;

    while (numBytesRead < chunkSize) {
      int pendingBytes = calculatePendingBytes(numBytesRead, instanceBuffer.length, chunkSize);
      log.info("Reading from: {} to: {}", 0, pendingBytes);
      int read = inputStream.read(instanceBuffer, 0, pendingBytes);
      log.info("Read complete, num bytes: {}", read);

      if (read == -1) {
        log.error("Unexpected break: {}", read);
        break;
      }

      if (read > 0) {
        numBytesRead += read;
        outputStream.write(instanceBuffer, 0, read);
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


  private void streamHeaders() throws Exception {
    while (true) {
      byte[] data = readLineBytes();

      log.debug("Read line: {}", new String(data));
      if (data.length < 1) {
        break;
      }

      this.headerProcessor.writeHeader(data, this.outputStream);
      this.outputStream.write(CRLF.getBytes());
    }
  }

  public byte[] readLineBytes() throws IOException {
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
        instanceBuffer[index] = (byte)readItem;
        index++;
        readItem = inputStream.read();
      }
    }

    byte[] fullBuffer = new byte[index];
    System.arraycopy(instanceBuffer, 0, fullBuffer, 0, index);
    return fullBuffer;
  }
}
