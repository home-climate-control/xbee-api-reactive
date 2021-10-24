package com.rapplogic.xbee.examples;

import com.rapplogic.xbee.api.IPacketParser;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ResponseHandlerExample {

	@Test
	void testResponseHandlerExample() {
		XBee xbee = new XBee();
		XBee.registerResponseHandler(0x88, MyResponse.class);
//		xbee.open(..);
	}

	public static class MyResponse extends XBeeResponse {

		@Override
		protected void parse(IPacketParser parser) throws IOException {
//			this.setxxx(parser.read("AT Response Frame Id"));
//			this.setxxy(parser.read("AT Response Char 1"));
//			this.setxxz(parser.read("AT Response Char 2"));
		}
	}
}
