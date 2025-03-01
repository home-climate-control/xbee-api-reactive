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

import com.homeclimatecontrol.xbee.AddressParser;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * Represents a 64-bit XBee Address
 * <p/>
 * @author andrew
 *
 */
public class XBeeAddress64 extends XBeeAddress {

    public static final XBeeAddress64 BROADCAST = new XBeeAddress64(new int[]{0, 0, 0, 0, 0, 0, 0xff, 0xff});
    public static final XBeeAddress64 ZNET_COORDINATOR = new XBeeAddress64(new int[]{0, 0, 0, 0, 0, 0, 0, 0});

    private int[] address;

    /**
     * Parses an 64-bit XBee address from a string representation
     * May be contain spaces ## ## ## ## ## ## ## ## or without ################ but cannot use the 0x prefix
     * ex: 0013A200408B98FF or 00 13 A2 00 40 8B 98 FF
     */
    public XBeeAddress64(String addressStr) {
        address = new int[8];

        if (addressStr.contains(" ")) {
            StringTokenizer st = new StringTokenizer(addressStr, " ");

            for (int i = 0; i < address.length; i++) {
                String byteStr = st.nextToken();
                address[i] = Integer.parseInt(byteStr, 16);
            }
        } else {
            // secretly also handle no space format
            for (int i = 0; i < address.length; i++) {
                address[i] = Integer.parseInt(addressStr.substring(i * 2, i * 2 + 2), 16);
            }
        }
    }

    /**
     * Creates a 64-bit address
     *
     * @param b1 MSB
     * @param b2 byte
     * @param b3 byte
     * @param b4 byte
     * @param b5 byte
     * @param b6 byte
     * @param b7 byte
     * @param b8 LSB
     */
    public XBeeAddress64(int b1, int b2, int b3, int b4, int b5, int b6, int b7, int b8) {
        address = new int[8];

        address[0] = b1;
        address[1] = b2;
        address[2] = b3;
        address[3] = b4;
        address[4] = b5;
        address[5] = b6;
        address[6] = b7;
        address[7] = b8;
    }

    public XBeeAddress64(int[] address) {
        this.address = address;
    }

    public XBeeAddress64() {
        address = new int[8];
    }

    public XBeeAddress64(ByteBuffer source) {

        address = new int[8];

        for (var offset = 0; offset < 8; offset++) {
            address[offset] = source.get() & 0xFF;
        }
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof XBeeAddress64)) {
            return false;
        }

        XBeeAddress64 that = (XBeeAddress64) o;

        return Arrays.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return address != null ? Arrays.hashCode(address) : 0;
    }

    @Override
    public int[] getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return AddressParser.render4x4(this);
    }
}
