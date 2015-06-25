package com.rapplogic.xbee;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeException;

/**
 * Tests opening and closing connections to the radio
 * 
 * @author andrew
 *
 */
public class OpenCloseConnectionsTest extends TestCase {

	private final static Logger log = Logger.getLogger(OpenCloseConnectionsTest.class);
	
	private XBee xbee = new XBee();
	
	public void testOpenCloseConnections() throws XBeeException, InterruptedException {
		
		// series 1
		String device = "/dev/tty.usbserial-A4004Rim";
		// series 2
//		String device = "/dev/tty.usbserial-A6005v5M";
		
		log.info("opening connection");
		
//		xbee.setStartupChecks(false);
		
		// first connect directly to end device and configure.  then comment out configureXXX methods and connect to coordinator
		xbee.open(device, 9600);
		
		if (!xbee.isConnected()) {
		    fail("Should be connected");
		}
		
		try {
			log.info("attempting duplicate open");
			xbee.open(device, 9600);
			fail("already open");
			
		} catch (Exception e) {
			log.debug("Expected", e);
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
			
		} catch (Exception e) {
			log.debug("Expected", e);
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
			
		} catch (Exception e) {
			log.debug("Expected", e);
		}
	}	
}
