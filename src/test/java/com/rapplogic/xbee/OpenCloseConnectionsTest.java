package com.rapplogic.xbee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
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

	/**
	 * Test the functionality with the actual serial port.
	 * 
	 * Enable this test only if you have the hardware connected.
	 */
	@Test
	@Ignore
	public void testSerial() throws XBeeException, InterruptedException, IOException {

		// series 1 (VT: FIXME: series of what?)
		// String port = "/dev/tty.usbserial-A4004Rim";

		// series 2 (VT: FIXME: series of what?)
		// String port = "/dev/tty.usbserial-A6005v5M";

		// x86
		String port = "/dev/ttyUSB0";

		testOpenCloseConnections(port);
	}

	private void testOpenCloseConnections(String port) throws XBeeException, InterruptedException, IOException {
		NDC.push("testOpenClose(" + port + ")");

		try {
			
			testConnection(new XBeeSerialConnectionWrapper(port, 9600));

		} finally {
			NDC.pop();
		}
	}
	
	private void testConnection(XBeeConnectionWrapper connectionWrapper) throws XBeeException, IOException {
		
		log.info("opening connection");

		XBeeConnection connection = connectionWrapper.open();
		
		// first connect directly to end device and configure.  then comment out configureXXX methods and connect to coordinator
		xbee.initProviderConnection(connection);

		if (!xbee.isConnected()) {
			fail("Should be connected");
		}

		try {

			log.info("attempting duplicate open");
			xbee.initProviderConnection(connection);
			fail("already open");

		} catch (Throwable t) {

			// VT: FIXME: https://github.com/home-climate-control/xbee-api/issues/1

			assertSame("Wrong exception class", IllegalStateException.class, t.getClass());
			assertEquals("Wrong exception message", "Cannot open new connection -- existing connection is still open.  Please close first", t.getMessage());
		}

		log.info("sending channel command");

		if (xbee.sendSynchronous(new AtCommand("CH")).isError()) {
			fail("fail");
		}

		log.info("closing connection");
		xbee.close();

		if (xbee.isConnected()) {
			fail("Should be disconnected");
		}

		try {

			log.info("sending at command, but we're disconnected");
			xbee.sendSynchronous(new AtCommand("CH"));
			fail("Should be disconnected");

		} catch (Throwable t) {

			// VT: FIXME: https://github.com/home-climate-control/xbee-api/issues/1

			assertSame("Wrong exception class", XBeeNotConnectedException.class, t.getClass());
			assertEquals("Wrong exception message", null, t.getMessage());
		}

		log.info("reconnecting");
		xbee.initProviderConnection(connectionWrapper.reopen());

		if (xbee.sendSynchronous(new AtCommand("CH")).isError()) {
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
	
	private abstract static class XBeeConnectionWrapper {
		
		abstract XBeeConnection open() throws IOException;
		abstract XBeeConnection reopen() throws IOException;
	}
	
	private static class XBeeSerialConnectionWrapper extends XBeeConnectionWrapper {

		public final String port;
		public final int baudRate;
		
		private final SerialPortConnection serial = new SerialPortConnection();
		
		public XBeeSerialConnectionWrapper(String port, int baudRate) {
			
			this.port = port;
			this.baudRate = baudRate;
		}
		
		@Override
		XBeeConnection open() throws IOException {

			try {
				
				serial.openSerialPort(port, baudRate);
				
				return serial;
				
			} catch (Throwable t) {
				throw new IOException("Oops", t);
			}
		}

		@Override
		XBeeConnection reopen() throws IOException {
			
			// VT: FIXME: Is there a way to assert that the connection is now closed?
			
			return open();
		}
	}
}
