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

import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.rapplogic.xbee.TestPortProvider.getTestPort;

/**
 * @author andrew
 */
class BroadcastReceiverExample {

	private final Logger log = LogManager.getLogger(getClass());

	@Test
    @Disabled("Enable only if safe to use hardware is connected")
	void testBroadcastReceiverExample() throws XBeeException {

		XBee xbee = new XBee();

		try {
			// An XBee with Coordinator firmware must be connected to this port
			xbee.open(getTestPort(), 9600);

			while (true) {
				XBeeResponse response = xbee.getResponse();
				log.info("received response " + response);
			}
		} finally {
			if (xbee != null && xbee.isConnected()) {
				xbee.close();
			}
		}
	}
}
