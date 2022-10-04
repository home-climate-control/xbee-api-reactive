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

package com.rapplogic.xbee.api;

import java.io.IOException;


//TODO Now supported by series 1 XBee. parseIoSample now needs to handle series 1 and 2

/**
 * Supported by both series 1 (10C8 firmware and later) and series 2.
 * Represents a response, corresponding to a RemoteAtRequest.
 * <p/>
 * API ID: 0x97
 */
public class RemoteAtResponse extends AtCommandResponse {

    private XBeeAddress64 remoteAddress64;
    private XBeeAddress16 remoteAddress16;

    public void setRemoteAddress64(
            XBeeAddress64 sixtyFourBitResponderAddress) {
        remoteAddress64 = sixtyFourBitResponderAddress;
    }

    public void setRemoteAddress16(
            XBeeAddress16 sixteenBitResponderAddress) {
        remoteAddress16 = sixteenBitResponderAddress;
    }

    @Override
    public void parse(IPacketParser parser) throws IOException {
        setFrameId(parser.read("Remote AT Response Frame Id"));

        setRemoteAddress64(parser.parseAddress64());

        setRemoteAddress16(parser.parseAddress16());

        char cmd1 = (char) parser.read("Command char 1");
        char cmd2 = (char) parser.read("Command char 2");

        setChar1(cmd1);
        setChar2(cmd2);

        int status = parser.read("AT Response Status");
        setStatus(AtCommandResponse.Status.get(status));

        setValue(parser.readRemainingBytes());
    }

    @Override
    public String toString() {
        return super.toString() +
                ",remoteAddress64=" + remoteAddress64 +
                ",remoteAddress16=" + remoteAddress16;
    }
}
