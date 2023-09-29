package org.reboot.server.secure.core.stream;

import org.reboot.server.secure.model.HeaderProcessingResponse;
import org.reboot.server.secure.model.HttpHeaderContext;
import org.reboot.server.secure.model.StreamContext;

import java.io.OutputStream;

public interface IHeaderProcessor {

  HeaderProcessingResponse processHeader(byte[] data, int limit, HttpHeaderContext httpHeaderContext, StreamContext streamContext) throws Exception ;

}
