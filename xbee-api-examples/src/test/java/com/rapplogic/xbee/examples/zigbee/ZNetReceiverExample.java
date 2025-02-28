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
import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.util.ByteUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.rapplogic.xbee.TestPortProvider.getTestPort;
import static com.rapplogic.xbee.api.AtCommand.Command.DB;

/**
 * This class is the companion to ZNetSenderTest.java, and as such, it receives packets sent by ZNetSenderTest.java
 * See the ZNetSenderTest.java for information on how to configure your XBee for this demo
 *
 * You can start ZNetSenderTest.java and this class in any order but it's generally best to start this class first.
 *
 * @author andrew
 *
 */
class ZNetReceiverExample {

    private final Logger log = LogManager.getLogger(getClass());

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void testZNetReceiverExample() throws Exception {

        XBee xbee = new XBee();

        try {
            // replace with the com port of your receiving XBee (typically your end device)
            // router
            xbee.open(getTestPort(), 9600);

            while (true) {

                try {
                    // we wait here until a packet is received.
                    XBeeResponse response = xbee.getResponse();

                    log.info("received response " + response.toString());

                    if (response.getApiId() == ApiId.ZNET_RX_RESPONSE) {
                        // we received a packet from ZNetSenderTest.java
                        ZNetRxResponse rx = (ZNetRxResponse) response;

                        log.info("Received RX packet, option is " + rx.getOption() + ", sender 64 address is " + ByteUtils.toBase16(rx.getRemoteAddress64().getAddress()) + ", remote 16-bit address is " + ByteUtils.toBase16(rx.getRemoteAddress16().getAddress()) + ", data is " + ByteUtils.toBase16(rx.getData()));

                        // optionally we may want to get the signal strength (RSSI) of the last hop.
                        // keep in mind if you have routers in your network, this will be the signal of the last hop.
                        AtCommand at = new AtCommand(DB);
                        xbee.sendAsynchronous(at);
                        XBeeResponse atResponse = xbee.getResponse();

                        if (atResponse.getApiId() == ApiId.AT_RESPONSE) {
                            // remember rssi is a negative db value
                            log.info("RSSI of last response is " + -((AtCommandResponse) atResponse).getValue()[0]);
                        } else {
                            // we didn't get an AT response
                            log.info("expected RSSI, but received " + atResponse.toString());
                        }
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
