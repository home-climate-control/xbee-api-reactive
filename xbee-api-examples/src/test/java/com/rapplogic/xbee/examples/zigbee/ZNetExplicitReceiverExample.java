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
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetExplicitRxResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.rapplogic.xbee.TestPortProvider.getTestPort;

/**
 * Set AO=1 for to enable explicit frames for this example
 *
 * @author andrew
 *
 */
class ZNetExplicitReceiverExample {

    private final Logger log = LogManager.getLogger(getClass());

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void testZNetExplicitReceiverExample() throws Exception {
        XBee xbee = new XBee();

        try {
            // replace with the com port or your receiving XBee
            xbee.open(getTestPort(), 9600);

            while (true) {

                try {
                    // we wait here until a packet is received.
                    XBeeResponse response = xbee.getResponse();

                    if (response.getApiId() == ApiId.ZNET_EXPLICIT_RX_RESPONSE) {
                        ZNetExplicitRxResponse rx = (ZNetExplicitRxResponse) response;

                        log.info("received explicit packet response " + response.toString());
                    } else {
                        log.debug("received unexpected packet " + response.toString());
                    }
                } catch (Throwable t) {
                    log.error("Unexpected exception", t);
                }
            }
        } finally {
            if (xbee != null && xbee.isConnected()) {
                xbee.close();
            }
        }
    }
}
