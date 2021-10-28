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

import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeConfiguration;
import com.rapplogic.xbee.api.XBeeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.rapplogic.xbee.TestPortProvider.getTestPort;
import static com.rapplogic.xbee.api.AtCommand.Command.MY;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The AtCommand/AtCommandResponse classes are supported by both ZNet and WPAN XBees but certain
 * commands are specific to ZNet or WPAN.
 *
 * Commands that are ZNet specific are located in the ZNetApiAtTest.
 *
 * Refer to the manual for more information on available commands
 *
 * @author andrew
 *
 */
class ApiAtExample {

//	TODO split class in to WPAN class

	private final Logger log = LogManager.getLogger(getClass());

	private final XBee xbee = new XBee(new XBeeConfiguration().withStartupChecks(false));

	@Test
    @Disabled("Enable only if safe to use hardware is connected")
	void testApiAtExample() throws XBeeException {

		try {
			xbee.open(getTestPort(), 9600);

//			// set D1 analog input
//			this.sendCommand(new AtCommand("D1", 2));
//			// set D2 digital input
//			this.sendCommand(new AtCommand("D2", 3));
//			// send sample every 5 seconds
//			this.sendCommand(new AtCommand("IR", new int[] {0x13, 0x88}));

			log.info("MY is " + xbee.sendSynchronousAT(new AtCommand(MY)));
//			log.info("SH is " + xbee.sendAtCommand(new AtCommand("SH")));
//			log.info("SL is " + xbee.sendAtCommand(new AtCommand("SL")));
		} catch (Throwable t) {

			log.error("at command failed", t);
			fail("Failed, check the logs");

		} finally {
			if (xbee != null && xbee.isConnected()) {
				xbee.close();
			}
		}
	}
}
