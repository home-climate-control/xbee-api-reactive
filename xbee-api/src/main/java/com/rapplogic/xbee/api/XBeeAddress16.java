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

import com.rapplogic.xbee.util.DoubleByte;

import java.nio.ByteBuffer;

/**
 * Represents a 16-bit XBee Address.
 * <p/>
 * @author andrew
 *
 */
public class XBeeAddress16 extends XBeeAddress {

	public static final XBeeAddress16 BROADCAST = new XBeeAddress16(0xFF, 0xFF);
	public static final XBeeAddress16 ZNET_BROADCAST = new XBeeAddress16(0xFF, 0xFE);

	private final DoubleByte doubleByte = new DoubleByte();

	/**
	 * Provide address as msb byte and lsb byte
	 */
	public XBeeAddress16(int msb, int lsb) {
		doubleByte.setMsb(msb);
		doubleByte.setLsb(lsb);
	}

	public XBeeAddress16(int[] arr) {
		doubleByte.setMsb(arr[0]);
		doubleByte.setLsb(arr[1]);
	}

	public XBeeAddress16() {

	}

    public XBeeAddress16(ByteBuffer source) {
        doubleByte.setMsb(source.get() & 0xFF);
        doubleByte.setLsb(source.get() & 0xFF);
    }

    public int getMsb() {
		return doubleByte.getMsb();
	}

	public void setMsb(int msb) {
		doubleByte.setMsb(msb);
	}

	public int getLsb() {
		return doubleByte.getLsb();
	}

	public void setLsb(int lsb) {
		doubleByte.setLsb(lsb);
	}

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof XBeeAddress16)) {
            return false;
        }

        XBeeAddress16 that = (XBeeAddress16) o;

        return doubleByte.equals(that.doubleByte);
    }

    @Override
    public int hashCode() {
        return doubleByte.hashCode();
    }

    @Override
	public int[] getAddress() {
		return new int[] { doubleByte.getMsb(), doubleByte.getLsb() };
	}
}
