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
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetExplicitTxRequest;
import com.rapplogic.xbee.api.zigbee.ZNetTxRequest;
import com.rapplogic.xbee.util.DoubleByte;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.rapplogic.xbee.TestPortProvider.getTestPort;

/**
 * Set AO=1 for to enable explicit frames for this example.
 * Once set, you should use explicit tx/rx packets instead of plain vanilla tx requests (ZNetTxRequest).
 * You can still send ZNetTxRequest requests but they will be received as explicit responses (ZNetExplicitRxResponse)
 *
 * @author andrew
 *
 */
class ZNetExplicitSenderExample {

    private final Logger log = LogManager.getLogger(getClass());

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void testZNetExplicitSenderExample() throws XBeeException {

        XBee xbee = new XBee();

        try {
            // An XBee with Coordinator firmware must be connected to this port
            xbee.open(getTestPort(), 9600);

            // replace with end device's 64-bit address (SH + SL)
            XBeeAddress64 addr64 = new XBeeAddress64(0, 0x13, 0xa2, 0, 0x40, 0x0a, 0x3e, 0x02);

            // create an array of arbitrary data to send
            int[] payload = new int[]{0, 0x66, 0xee};

            // loopback test
            int sourceEndpoint = 0;
            int destinationEndpoint = ZNetExplicitTxRequest.Endpoint.DATA.getValue();

            DoubleByte clusterId = new DoubleByte(0x0, ZNetExplicitTxRequest.ClusterId.SERIAL_LOOPBACK.getValue());
            //DoubleByte clusterId = new DoubleByte(0x0, ZNetExplicitTxRequest.ClusterId.TRANSPARENT_SERIAL.getValue());

            // first request we just send 64-bit address.  we get 16-bit network address with status response
            ZNetExplicitTxRequest request = new ZNetExplicitTxRequest((byte) 0xFF, addr64, XBeeAddress16.ZNET_BROADCAST,
                    ZNetTxRequest.DEFAULT_BROADCAST_RADIUS, ZNetTxRequest.Option.UNICAST, payload, sourceEndpoint, destinationEndpoint, clusterId, ZNetExplicitTxRequest.znetProfileId);

            log.info("sending explicit " + request);

            while (true) {
                xbee.sendAsynchronous(request);

                XBeeResponse response = xbee.getResponse();

                log.info("received response " + response.toString());

                try {
                    // wait a bit then send another packet
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
            }
        } finally {
            if (xbee != null && xbee.isConnected()) {
                xbee.close();
            }
        }
    }
}
