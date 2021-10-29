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

package com.rapplogic.xbee.examples.wpan;

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.ErrorResponse;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.RxResponse16;
import com.rapplogic.xbee.api.wpan.RxResponse64;
import com.rapplogic.xbee.util.ByteUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.rapplogic.xbee.TestPortProvider.getTestPort;

/**
 * Receives IO samples from remote radio
 * I have a photoresistor connected to analog0 and a thermistor is connected to analog1
 * Also there is a breadboard switch connected to digital2 with change detect configured
 *
 * @author andrew
 *
 */
class ApiReceiverExample {

	private final Logger log = LogManager.getLogger(getClass());

	@Test
    @Disabled("Enable only if safe to use hardware is connected")
	void testApiReceiverExample() throws Exception {

		try (var xbee = new XBee()) {

            // my end device
			xbee.open(getTestPort(), 9600);
			// my coordinator
			//xbee.open("/dev/tty.usbserial-A4004Rim", 9600);

            int count = 0;
            int errors = 0;

            while (true) {

				try {
					XBeeResponse response = xbee.getResponse();
					count++;

					if (response.isError()) {
						log.info("response contains errors", ((ErrorResponse)response).getException());
						errors++;
					}

					for (int i = 0; i < response.getRawPacketBytes().length; i++) {
						log.info("packet [" + i + "] " + ByteUtils.toBase16(response.getRawPacketBytes()[i]));
					}

 					if (response.getApiId() == ApiId.RX_16_RESPONSE) {
                        assert response instanceof RxResponse16;
                        log.info("Received RX 16 packet " + ((RxResponse16)response));
 					} else if (response.getApiId() == ApiId.RX_64_RESPONSE) {
                        assert response instanceof RxResponse64;
                        log.info("Received RX 64 packet " + ((RxResponse64)response));
					} else {
						log.info("Ignoring mystery packet " + response);
					}

					log.debug("Received response: " + response + ", count is " + count + ", errors is " + errors);

				} catch (Throwable t) {
					log.error("Unexpected exception", t);
				}
			}
		}
	}
}
