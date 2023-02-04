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

package com.rapplogic.xbee.api.zigbee;

import com.homeclimatecontrol.xbee.AddressParser;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.util.ByteUtils;
import com.rapplogic.xbee.util.IntArrayInputStream;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static com.rapplogic.xbee.api.AtCommand.Command.ND;

/**
 * Series 2 XBee.  Parses a Node Discover (ND) AT Command Response
 * <p/>
 * @author andrew
 *
 */
public class ZBNodeDiscover {

	public enum DeviceType {
		COORDINATOR(0),
		ROUTER(1),
		END_DEVICE(2);

		private static final Map<Integer,DeviceType> lookup = new HashMap<>();

		static {
			for(DeviceType s : EnumSet.allOf(DeviceType.class)) {
				lookup.put(s.getValue(), s);
			}
		}

		public static DeviceType get(int value) {
			return lookup.get(value);
		}

	    private final int value;

	    DeviceType(int value) {
	        this.value = value;
	    }

		public int getValue() {
			return value;
		}
	}

	private XBeeAddress16 nodeAddress16;
	private XBeeAddress64 nodeAddress64;
	private String nodeIdentifier;
	private XBeeAddress16 parent;
	private DeviceType type;
	private int status;
	private int[] profileId;
	private int[] mfgId;

	public DeviceType getType() {
		return type;
	}


	public void setType(DeviceType type) {
		this.type = type;
	}


	public int getStatus() {
		return status;
	}


	public void setStatus(int status) {
		this.status = status;
	}


	public int[] getProfileId() {
		return profileId;
	}


	public void setProfileId(int[] profileId) {
		this.profileId = profileId;
	}


	public int[] getMfgId() {
		return mfgId;
	}


	public void setMfgId(int[] mfgId) {
		this.mfgId = mfgId;
	}


	public static ZBNodeDiscover parse(AtCommandResponse response) {

		if (!response.getCommand().equals(ND)) {
			throw new IllegalArgumentException("This method is only applicable for the ND command, given: " + response);
		}

		int[] data = response.getValue();

		IntArrayInputStream in = new IntArrayInputStream(data);

		ZBNodeDiscover nd = new ZBNodeDiscover();

		nd.setNodeAddress16(new XBeeAddress16(in.read(2)));

		nd.setNodeAddress64(new XBeeAddress64(in.read(8)));

		var ni = new StringBuilder();

		int ch;

		// NI is terminated with 0
		while ((ch = in.read()) != 0) {
			if (ch < 32 || ch > 126) {
				throw new IllegalArgumentException("Node Identifier '" + ch + "' is non-ASCII in: " + response);
			}

			ni.append((char)ch);
		}

		nd.setNodeIdentifier(ni.toString());

		nd.setParent(new XBeeAddress16(in.read(2)));
		nd.setType(DeviceType.get(in.read()));
		// TODO this is being reported as 1 (router) for my end device
		nd.setStatus(in.read());
		nd.setProfileId(in.read(2));
		nd.setMfgId(in.read(2));

		return nd;
	}

	@Override
    public String toString() {
		return "nodeAddress16=" + nodeAddress16 +
		", nodeAddress64=" + AddressParser.render4x4(nodeAddress64) +
		", nodeIdentifier=" + nodeIdentifier +
		", parentAddress=" + getParent() +
		", deviceType=" + type +
		", status=" + status +
		", profileId=" + ByteUtils.toBase16(profileId) +
		", mfgId=" + ByteUtils.toBase16(mfgId);
	}


	public XBeeAddress16 getNodeAddress16() {
		return nodeAddress16;
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


	public XBeeAddress16 getParent() {
		return parent;
	}


	public void setParent(XBeeAddress16 parent) {
		this.parent = parent;
	}
}
