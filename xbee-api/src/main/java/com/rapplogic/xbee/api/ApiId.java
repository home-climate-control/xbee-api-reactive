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

import com.rapplogic.xbee.XBeeFrameDirection;
import com.rapplogic.xbee.util.ByteUtils;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * XBee frame directions.
 *
 * See <a href="https://www.digi.com/resources/documentation/Digidocs/90001942-13/reference/r_supported_frames_zigbee.htm">Supported Frames</a>.
 *
 * @author andrew
 * @author Copyright &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2021
 */
public enum ApiId {

    // VT: FIXME: Who are these guys? Do they belong here?
	TX_REQUEST_64 (0x0, null),
	TX_REQUEST_16 (0x1, null),
    RX_64_RESPONSE (0x80, null),
    RX_16_RESPONSE (0x81, null),
    RX_64_IO_RESPONSE (0x82, null),
    RX_16_IO_RESPONSE (0x83, null),
    TX_STATUS_RESPONSE (0x89, null),

	AT_COMMAND (0x08, XBeeFrameDirection.TRANSMIT),
	AT_COMMAND_QUEUE (0x09, XBeeFrameDirection.TRANSMIT),
    ZNET_TX_REQUEST (0x10, XBeeFrameDirection.TRANSMIT),
    ZNET_EXPLICIT_TX_REQUEST (0x11, XBeeFrameDirection.TRANSMIT),
	REMOTE_AT_REQUEST (0x17, XBeeFrameDirection.TRANSMIT),


	AT_RESPONSE (0x88, XBeeFrameDirection.RECEIVE),
	MODEM_STATUS_RESPONSE (0x8A, XBeeFrameDirection.RECEIVE),
    ZNET_TX_STATUS_RESPONSE (0x8B, XBeeFrameDirection.RECEIVE),
	ZNET_RX_RESPONSE (0x90, XBeeFrameDirection.RECEIVE),
	ZNET_EXPLICIT_RX_RESPONSE (0x91, XBeeFrameDirection.RECEIVE),
    ZNET_IO_SAMPLE_RESPONSE (0x92, XBeeFrameDirection.RECEIVE),
    ZNET_IO_NODE_IDENTIFIER_RESPONSE (0x95, XBeeFrameDirection.RECEIVE),
	REMOTE_AT_RESPONSE (0x97, XBeeFrameDirection.RECEIVE),

	/**
	 * Indicates that we've parsed a packet for which we didn't know how to handle the API type.  This will be parsed into a GenericResponse
	 */
	UNKNOWN (0xFF, null),

	/**
	 * This is returned if an error occurs during packet parsing and does not correspond to a XBee API ID.
	 */
	ERROR_RESPONSE (-1, null);

    public final int id;

    /**
     * Frame direction; {@code null} value indicates unknown.
     */
    public final XBeeFrameDirection direction;

	private static final Map<Integer, ApiId> lookup = new HashMap<>();

    ApiId(int id, XBeeFrameDirection direction) {
        this.id = id;
        this.direction = direction;
    }

	static {
		for(ApiId s : EnumSet.allOf(ApiId.class)) {
			lookup.put(s.getId(), s);
		}
	}

	public static ApiId get(int value) {
		return lookup.get(value);
	}



	public int getId() {
		return id;
	}

    @Override
	public String toString() {
		return name() + " (" + ByteUtils.toBase16(getId()) + "," + direction + ")";
	}
}
