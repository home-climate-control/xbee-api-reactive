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

package com.rapplogic.xbee.api.wpan;

import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.util.IntArrayInputStream;

import static com.rapplogic.xbee.api.AtCommand.Command.ND;

// tested ok via xmpp on 4/13/09

/**
 * Series 1 XBee.  Parses a Node Discover (ND) AT Command Response
 */
public class WpanNodeDiscover {

	private XBeeAddress16 nodeAddress16;
	private XBeeAddress64 nodeAddress64;
	private int rssi;
	private String nodeIdentifier;

	public static WpanNodeDiscover parse(AtCommandResponse response) {

		if (!response.getCommand().equals(ND)) {
			throw new IllegalArgumentException("This method is only applicable for the ND command");
		}

		int[] data = response.getValue();

		if (data == null || data.length == 0) {
			throw new IllegalArgumentException("ND command has no value");
		}

		IntArrayInputStream in = new IntArrayInputStream(data);

		WpanNodeDiscover nd = new WpanNodeDiscover();

		nd.setNodeAddress16(new XBeeAddress16(in.read(2)));

		nd.setNodeAddress64(new XBeeAddress64(in.read(8)));

		nd.setRssi(-1*in.read());

		StringBuilder ni = new StringBuilder();

		int ch;

		// NI is terminated with 0
		while ((ch = in.read()) != 0) {
			if (ch < 32 || ch > 126) {
				throw new RuntimeException("Node Identifier " + ch + " is non-ascii");
			}

			ni.append((char)ch);
		}

		nd.setNodeIdentifier(ni.toString());

		return nd;
	}

	@Override
    public String toString() {
		return "nodeAddress16=" + nodeAddress16 +
		", nodeAddress64=" + nodeAddress64 +
		", rssi=" + rssi +
		", nodeIdentifier=" + nodeIdentifier;
	}

	public void setNodeAddress16(XBeeAddress16 my) {
		nodeAddress16 = my;
	}

	public XBeeAddress64 getNodeAddress64() {
		return nodeAddress64;
	}

	public void setNodeAddress64(XBeeAddress64 serial) {
		nodeAddress64 = serial;
	}

	public String getNodeIdentifier() {
		return nodeIdentifier;
	}

	public void setNodeIdentifier(String nodeIdentifier) {
		this.nodeIdentifier = nodeIdentifier;
	}

	public int getRssi() {
		return rssi;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
	}
}
