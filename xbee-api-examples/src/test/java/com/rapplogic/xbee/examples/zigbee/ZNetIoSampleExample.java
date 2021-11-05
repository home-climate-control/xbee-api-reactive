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

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.rapplogic.xbee.TestPortProvider.getTestPort;

/**
 * Series 2 XBees -- Example of receiving I/O samples.  To configure your radio for this example, connect
 * your end device to your serial connection and run the configureIOSamples() method
 * in ZNetApiAtTest.
 *
 * @author andrew
 *
 */
class ZNetIoSampleExample implements PacketListener {

	private final Logger log = LogManager.getLogger(getClass());

	@Test
    @Disabled("Enable only if safe to use hardware is connected")
	void testZNetIoSampleExample() throws Exception {

		XBee xbee = new XBee();

		try {
            // An XBee with Coordinator firmware must be connected to this port
			xbee.open(getTestPort(), 9600);
			xbee.addPacketListener(this);

			// wait forever
			synchronized(this) { wait(); }
		} finally {
			if (xbee != null && xbee.isConnected()) {
				xbee.close();
			}
		}
	}

	/**
	 * Called by XBee API thread when a packet is received
	 */
	@Override
	public void processResponse(XBeeResponse response) {
		// This is a I/O sample response.  You will only get this is you are connected to a Coordinator that is configured to
		// receive I/O samples from a remote XBee.

		if (response.getApiId() == ApiId.ZNET_IO_SAMPLE_RESPONSE) {
			ZNetRxIoSampleResponse ioSample = (ZNetRxIoSampleResponse) response;

			log.debug("received i/o sample packet.  contains analog is " + ioSample.containsAnalog() + ", contains digital is " + ioSample.containsDigital());

			// check the value of the input pins
			log.debug("pin 20 (DO) digital is " + ioSample.isDigitalOn(0).get());
			log.debug("pin 19 (D1) analog is " + ioSample.getAnalog1());
		}
	}
}
