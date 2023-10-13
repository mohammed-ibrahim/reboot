package org.reboot.server.secure.core.stream;

import org.reboot.server.secure.model.HeaderProcessingResponse;
import org.reboot.server.secure.model.HttpHeaderContext;
import org.reboot.server.secure.model.RequestContext;

import java.io.OutputStream;

public interface IHeaderProcessor {

  HeaderProcessingResponse processHeader(byte[] data, int limit, HttpHeaderContext httpHeaderContext, RequestContext requestContext) throws Exception ;

}
