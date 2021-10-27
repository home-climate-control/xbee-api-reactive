/*
 * Copyright (c) 2008 Andrew Rapp. All rights reserved.
 *
 * This file is part of XBee-API.
 *
 * XBee-API is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * XBee-API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with XBee-API.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.rapplogic.xbee.examples;

import com.rapplogic.xbee.api.RemoteAtRequest;
import com.rapplogic.xbee.api.RemoteAtResponse;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeTimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * This example uses Remote AT to turn on/off I/O pins.
 * This example is more interesting if you connect a LED to pin 20 on your end device.
 * Remember to use a resistor to limit the current flow.  I used a 215 Ohm resistor.
 * <p/>
 * Note: if your coordinator is powered on and receiving I/O samples, make sure you power off/on to drain
 * the traffic before running this example.
 *
 * @author andrew
 *
 */
class RemoteAtExample {

	private final Logger log = LogManager.getLogger(RemoteAtExample.class);

	@Test
    @Disabled("Enable only if safe to use hardware is connected")
	void testRemoteAtExample() throws XBeeException, InterruptedException {

		var xbee = new XBee();

		try {
			// replace with your coordinator com/baud
			// xbee.open("/dev/tty.usbserial-A6005v5M", 9600);
			// xbee.open("COM5", 9600);
			xbee.open("/dev/ttyUSB0", 9600);

			// replace with SH + SL of your end device
            var addr64 = new XBeeAddress64(0, 0x13, 0xa2, 0, 0x40, 0x0a, 0x3e, 0x02);

			// turn on end device (pin 20) D0 (Digital output high = 5)
			//RemoteAtRequest request = new RemoteAtRequest(addr64, "D0", new int[] {5});
			//RemoteAtRequest request = new RemoteAtRequest(addr64, "IR", new int[] {0x7f, 0xff});
			//RemoteAtRequest request = new RemoteAtRequest(addr64, "D5", new int[] {3});
			//RemoteAtRequest request = new RemoteAtRequest(addr64, "D0", new int[] {2});
			//RemoteAtRequest request = new RemoteAtRequest(addr64, "P2", new int[] {3});
            var request1 = new RemoteAtRequest(addr64, "P0", new int[] {1});

            var response1 = (RemoteAtResponse) xbee.sendSynchronous(request1, Duration.ofSeconds(10));

			if (response1.isOk()) {
				log.info("successfully turned on pin 20 (D0)");
			} else {
				fail("failed to turn on pin 20.  status is " + response1.getStatus());
			}

			System.exit(0);

			// wait a bit
			Thread.sleep(5000);
//
//			// now turn off end device D0
            var request2 = new RemoteAtRequest(addr64, "P0", new int[] {4});

			var response2 = (RemoteAtResponse) xbee.sendSynchronous(request2, Duration.ofSeconds(10));

			if (response2.isOk()) {
				log.info("successfully turned off pin 20 (D0)");
			} else {
				fail("failed to turn off pin 20.  status is " + response2.getStatus());
			}

		} catch (XBeeTimeoutException e) {
			log.error("request timed out. make sure you remote XBee is configured and powered on");
		} catch (Exception e) {
			log.error("unexpected error", e);
		} finally {
			if (xbee.isConnected()) {
				xbee.close();
			}
		}
	}
}
