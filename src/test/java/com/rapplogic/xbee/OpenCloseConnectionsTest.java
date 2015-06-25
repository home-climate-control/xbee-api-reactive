package com.rapplogic.xbee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeNotConnectedException;

/**
 * Tests opening and closing connections to the radio
 * 
 * @author andrew
 *
 */
public class OpenCloseConnectionsTest {

	private final static Logger log = Logger.getLogger(OpenCloseConnectionsTest.class);
	
	private XBee xbee = new XBee();
	
	@Test
	@Ignore
	public void testOpenCloseConnections() throws XBeeException, InterruptedException {
		
		// series 1 (VT: FIXME: series of what?)
		// String device = "/dev/tty.usbserial-A4004Rim";

		// series 2 (VT: FIXME: series of what?)
		// String device = "/dev/tty.usbserial-A6005v5M";
		
		// x86
		String device = "/dev/ttyUSB0";
		
		log.info("opening connection on " + device);
		
		// first connect directly to end device and configure.  then comment out configureXXX methods and connect to coordinator
		xbee.open(device, 9600);
		
		if (!xbee.isConnected()) {
		    fail("Should be connected");
		}
		
		try {
			
			log.info("attempting duplicate open");
			xbee.open(device, 9600);
			fail("already open");
			
		} catch (Throwable t) {

			assertSame("Wrong exception class", XBeeException.class, t.getClass());
			assertEquals("Wrong exception message", "java.lang.IllegalStateException: Cannot open new connection -- existing connection is still open.  Please close first", t.getMessage());
		}
		
		log.info("sending channel command");
		
		if (!xbee.sendAtCommand(new AtCommand("CH")).isOk()) {
		    fail("fail");
		}
		
		log.info("closing connection");
		xbee.close();
		
		if (xbee.isConnected()) {
			fail("Should be disconnected");
		}
		
		try {
			
			log.info("sending at command, but we're disconnected");
			xbee.sendAtCommand(new AtCommand("CH")).isOk();
			fail("Should be disconnected");
			
		} catch (Throwable t) {
			
			// VT: FIXME: https://github.com/home-climate-control/xbee-api/issues/1

			assertSame("Wrong exception class", XBeeNotConnectedException.class, t.getClass());
			assertEquals("Wrong exception message", null, t.getMessage());
		}
		
		log.info("reconnecting");
		xbee.open(device, 9600);
		
		if (!xbee.sendAtCommand(new AtCommand("CH")).isOk()) {
		    fail("fail");
		}
		
		log.info("closing conn");
		xbee.close();
		
		try {
			
			log.info("try duplicate close");
			xbee.close();
			fail("Already closed");
			
		} catch (Throwable t) {

			// VT: FIXME: https://github.com/home-climate-control/xbee-api/issues/1

			assertSame("Wrong exception class", IllegalStateException.class, t.getClass());
			assertEquals("Wrong exception message", "XBee is not connected", t.getMessage());
		}
	}	
}
