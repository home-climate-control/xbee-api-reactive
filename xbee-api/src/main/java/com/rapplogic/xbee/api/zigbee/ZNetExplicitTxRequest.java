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

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.util.ByteUtils;
import com.rapplogic.xbee.util.DoubleByte;
import com.rapplogic.xbee.util.IntArrayOutputStream;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Series 2 XBee.  Sends a packet to a remote radio.  The remote radio
 * receives the packet as a ZNetExplicitRxResponse packet.
 * <p/>
 * Radio must be configured for explicit frames to use this class (AO=1)
 * <p/>
 * API ID: 0x11
 *
 * @author andrew
 *
 */
public class ZNetExplicitTxRequest extends ZNetTxRequest {

	public enum Endpoint {
		ZDO_ENDPOINT(0),
		COMMAND (0xe6),
		DATA (0xe8);

		private static final Map<Integer,Endpoint> lookup = new HashMap<>();

		static {
			for(Endpoint s : EnumSet.allOf(Endpoint.class)) {
				lookup.put(s.getValue(), s);
			}
		}

		public static Endpoint get(int value) {
			return lookup.get(value);
		}

	    private final int value;

	    Endpoint(int value) {
	        this.value = value;
	    }

		public int getValue() {
			return value;
		}
	}

	public enum ClusterId {
		TRANSPARENT_SERIAL (Endpoint.DATA, 0x11),
		SERIAL_LOOPBACK (Endpoint.DATA, 0x12),
		IO_SAMPLE (Endpoint.DATA, 0x92),
		XBEE_SENSOR (Endpoint.DATA, 0x94),
		NODE_IDENTIFICATION (Endpoint.DATA, 0x95);

		private static final Map<Integer,ClusterId> lookup = new HashMap<>();

		static {
			for(ClusterId s : EnumSet.allOf(ClusterId.class)) {
				lookup.put(s.getValue(), s);
			}
		}

		public static ClusterId get(int value) {
			return lookup.get(value);
		}

	    private final int value;
	    private final Endpoint endpoint;

	    ClusterId(Endpoint endpoint, int value) {
	    	this.endpoint = endpoint;
	        this.value = value;
	    }

		public int getValue() {
			return value;
		}

		public Endpoint getEndpoint() {
			return endpoint;
		}
	}

	// TODO ZDO commands

	private int sourceEndpoint;
	private int destinationEndpoint;
	private DoubleByte clusterId;
	private DoubleByte profileId;

    public static final DoubleByte znetProfileId = new DoubleByte(0xc1, 0x05);
    public static final DoubleByte zdoProfileId = new DoubleByte(0, 0);

	public ZNetExplicitTxRequest(byte frameId, XBeeAddress64 dest64, XBeeAddress16 dest16, int broadcastRadius, ZNetTxRequest.Option option, int[] payload, 	int sourceEndpoint, int destinationEndpoint, DoubleByte clusterId, DoubleByte profileId) {
		super(frameId, dest64, dest16, broadcastRadius, option, payload);
		this.sourceEndpoint = sourceEndpoint;
		this.destinationEndpoint = destinationEndpoint;
		this.clusterId = clusterId;
		this.profileId = profileId;
	}

	/**
	 * Gets frame data from tx request (super) and inserts necessary bytes
	 */
    @Override
	public int[] getFrameData() {

		// get frame id from tx request
		IntArrayOutputStream frameData = getFrameDataAsIntArrayOutputStream();

		// overwrite api id
		frameData.getInternalList().set(0, getApiId().getId());

		// insert explicit bytes

		// source endpoint
		frameData.getInternalList().add(12, getSourceEndpoint());
		// dest endpoint
		frameData.getInternalList().add(13, getDestinationEndpoint());
		// cluster id msb
		frameData.getInternalList().add(14, getClusterId().getMsb());
		// cluster id lsb
		frameData.getInternalList().add(15, getClusterId().getLsb());
		// profile id
		frameData.getInternalList().add(16, getProfileId().getMsb());
		frameData.getInternalList().add(17, getProfileId().getLsb());

		return frameData.getIntArray();
	}

	@Override
    public ApiId getApiId() {
		return ApiId.ZNET_EXPLICIT_TX_REQUEST;
	}

	public int getSourceEndpoint() {
		return sourceEndpoint;
	}

    public int getDestinationEndpoint() {
		return destinationEndpoint;
	}

    public DoubleByte getClusterId() {
		return clusterId;
	}

    public DoubleByte getProfileId() {
		return profileId;
	}

    @Override
    public String toString() {
		return super.toString() +
			",sourceEndpoint=" + ByteUtils.toBase16(getSourceEndpoint()) +
			",destinationEndpoint=" + ByteUtils.toBase16(getDestinationEndpoint()) +
			",clusterId(msb)=" + ByteUtils.toBase16(getClusterId().getMsb()) +
			",clusterId(lsb)=" + ByteUtils.toBase16(getClusterId().getLsb()) +
			",profileId(msb)=" + ByteUtils.toBase16(getProfileId().getMsb()) +
			",profileId(lsb)=" + ByteUtils.toBase16(getProfileId().getLsb());
	}
}
