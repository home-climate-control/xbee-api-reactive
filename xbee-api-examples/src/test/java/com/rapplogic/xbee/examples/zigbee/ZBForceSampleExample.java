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

package com.rapplogic.xbee.examples.zigbee;

import com.rapplogic.xbee.api.RemoteAtResponse;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeRequest;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.XBeeTimeoutException;
import com.rapplogic.xbee.api.zigbee.ZBForceSampleRequest;
import com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.rapplogic.xbee.TestPortProvider.getTestPort;

/**
 * To run this sample you will need to configure pin 19 (D1) to Analog input on the remote XBee: D1=2.
 * To do this you can use X-CTU, the AtCommand [new AtCommand("D1", 2)] with your remote XBee connected to the serial port
 * or with RemoteAtRequest and send the request through the coordinator.
 *
 * @author andrew
 *
 */
class ZBForceSampleExample {

	private final Logger log = LogManager.getLogger(getClass());

	@Test
    @Disabled("Enable only if safe to use hardware is connected")
	void testZBForceSampleExample() throws Exception {

		XBee xbee = new XBee();

		try {
            // An XBee with Coordinator firmware must be connected to this port
			xbee.open(getTestPort(), 9600);

			while (true) {
				// All XBees allow you to request an I/O sample on a local XBee (serial connected), however this is not very interesting.
				// With ZNet/ZB Pro radios we can use Remote AT to force an I/O sample on an end device.
				// The following code issues a force sample on a XBee end device and parses the io sample.

				// replace with your end device 64-bit address
				XBeeAddress64 addr64 = new XBeeAddress64(0, 0x13, 0xa2, 0, 0x40, 0x0a, 0x3e, 0x02);

				XBeeRequest request = new ZBForceSampleRequest(addr64);

				try {
					XBeeResponse response = xbee.sendSynchronous(request, Duration.ofSeconds(6));
					RemoteAtResponse remoteAt = (RemoteAtResponse) response;

					if (remoteAt.isOk())  {
						// extract the i/o sample
						ZNetRxIoSampleResponse ioSample = ZNetRxIoSampleResponse.parseIsSample(remoteAt);
						// make sure you configured the remote XBee to D1=2 (analog input) or you will get an error
						log.info("10 bit analog1 sample is " + ioSample.getAnalog1());
					} else {
						log.info("Remote AT request failed: " + response);
					}
				} catch (XBeeTimeoutException e) {
					log.info("request timed out");
				}

				// wait a bit
				Thread.sleep(2000);
			}
		} finally {
			if (xbee != null && xbee.isConnected()) {
				xbee.close();
			}
		}
	}
}
