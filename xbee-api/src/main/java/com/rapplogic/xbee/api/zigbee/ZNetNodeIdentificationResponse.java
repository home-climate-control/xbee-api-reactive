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

import com.rapplogic.xbee.api.IPacketParser;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxBaseResponse.Option;
import com.rapplogic.xbee.util.DoubleByte;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class ZNetNodeIdentificationResponse extends XBeeResponse {

    private static final Logger log = LogManager.getLogger();

	// TODO this is repeated in NodeDiscover
	public enum DeviceType  {
		COORDINATOR (0x1),
		ROUTER (0x2),
		END_DEVICE (0x3),
		UNKNOWN(-1);

		private static final Map<Integer,DeviceType> lookup = new HashMap<>();

		static {
			for(DeviceType s: EnumSet.allOf(DeviceType.class)) {
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

	public enum SourceAction {
		PUSHBUTTON (0x1),
		JOINING (0x2),
		POWER_CYCLE(0x3),
		UNKNOWN(-1);

		private static final Map<Integer,SourceAction> lookup = new HashMap<>();

		static {
			for(SourceAction s: EnumSet.allOf(SourceAction.class)) {
				lookup.put(s.getValue(), s);
			}
		}

		public static SourceAction get(int value) {
			return lookup.get(value);
		}

	    private final int value;

	    SourceAction(int value) {
	        this.value = value;
	    }

		public int getValue() {
			return value;
		}
	}

	private XBeeAddress64 remoteAddress64;
	private XBeeAddress16 remoteAddress16;
	private Option option;
	// TODO Digi WTF why duplicated?? p.70
	private XBeeAddress64 remoteAddress64_2;
	private XBeeAddress16 remoteAddress16_2;

	private String nodeIdentifier;
	private XBeeAddress16 parentAddress;
	private DeviceType deviceType;
	private SourceAction sourceAction;
	private DoubleByte profileId;
	private DoubleByte mfgId;

	public XBeeAddress64 getRemoteAddress64() {
		return remoteAddress64;
	}

	public void setRemoteAddress64(XBeeAddress64 remoteAddress64) {
		this.remoteAddress64 = remoteAddress64;
	}

	public XBeeAddress16 getRemoteAddress16() {
		return remoteAddress16;
	}

	public void setRemoteAddress16(XBeeAddress16 remoteAddress16) {
		this.remoteAddress16 = remoteAddress16;
	}

	public Option getOption() {
		return option;
	}

	public void setOption(Option option) {
		this.option = option;
	}

	public void setRemoteAddress64_2(XBeeAddress64 remoteAddress64_2) {
		this.remoteAddress64_2 = remoteAddress64_2;
	}

	public void setRemoteAddress16_2(XBeeAddress16 remoteAddress16_2) {
		this.remoteAddress16_2 = remoteAddress16_2;
	}

	public String getNodeIdentifier() {
		return nodeIdentifier;
	}

	public void setNodeIdentifier(String nodeIdentifier) {
		this.nodeIdentifier = nodeIdentifier;
	}

	public XBeeAddress16 getParentAddress() {
		return parentAddress;
	}

	public void setParentAddress(XBeeAddress16 parentAddress) {
		this.parentAddress = parentAddress;
	}

	public DeviceType getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}

	public SourceAction getSourceAction() {
		return sourceAction;
	}

	public void setSourceAction(SourceAction sourceAction) {
		this.sourceAction = sourceAction;
	}

	public DoubleByte getProfileId() {
		return profileId;
	}

	public void setProfileId(DoubleByte profileId) {
		this.profileId = profileId;
	}

	public DoubleByte getMfgId() {
		return mfgId;
	}

	public void setMfgId(DoubleByte mfgId) {
		this.mfgId = mfgId;
	}

    @Override
	public void parse(IPacketParser parser) throws IOException {
		this.setRemoteAddress64(parser.parseAddress64());
		this.setRemoteAddress16(parser.parseAddress16());

		var option = parser.read("Option");

		this.setOption(ZNetRxBaseResponse.getOption(option));

		this.setRemoteAddress16_2(parser.parseAddress16());
		this.setRemoteAddress64_2(parser.parseAddress64());


		var ni = new StringBuilder();

		var ch = 0;

		// NI is terminated with 0
		while ((ch = parser.read("Node Identifier")) != 0) {
			ni.append((char)ch);
		}

		this.setNodeIdentifier(ni.toString());
		this.setParentAddress(parser.parseAddress16());

		var deviceType = parser.read("Device Type");

		if (DeviceType.get(deviceType) != null) {
			this.setDeviceType(DeviceType.get(deviceType));
		} else {
			log.warn("Unknown device type {}", deviceType);
			this.setDeviceType(DeviceType.UNKNOWN);
		}

		var sourceAction = parser.read("Source Action");

		if (SourceAction.get(sourceAction) != null) {
			this.setSourceAction(SourceAction.get(sourceAction));
		} else {
			log.warn("Unknown source event {}", sourceAction);
			this.setSourceAction(SourceAction.UNKNOWN);
		}


		var profileId = new DoubleByte();
		profileId.setMsb(parser.read("Profile MSB"));
		profileId.setLsb(parser.read("Profile LSB"));
		this.setProfileId(profileId);

		var mfgId = new DoubleByte();
		mfgId.setMsb(parser.read("MFG MSB"));
		mfgId.setLsb(parser.read("MFG LSB"));
		this.setMfgId(mfgId);
	}

	@Override
	public String toString() {
		return "ZNetNodeIdentificationResponse [deviceType=" + deviceType
				+ ", mfgId=" + mfgId + ", nodeIdentifier=" + nodeIdentifier
				+ ", option=" + option + ", parentAddress=" + parentAddress
				+ ", profileId=" + profileId + ", remoteAddress16="
				+ remoteAddress16 + ", remoteAddress16_2=" + remoteAddress16_2
				+ ", remoteAddress64=" + remoteAddress64
				+ ", remoteAddress64_2=" + remoteAddress64_2
				+ ", sourceAction=" + sourceAction + "]" +
				super.toString();
	}
}
