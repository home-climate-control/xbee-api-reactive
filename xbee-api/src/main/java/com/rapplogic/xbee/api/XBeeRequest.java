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

import java.io.Serializable;


/**
 * The super class of all XBee transmit packets.
 * Constructs frame data portion of an XBee packet
 * <p/>
 * @author andrew
 *
 */

public abstract class XBeeRequest implements Serializable {

	private static final long serialVersionUID = -9181542059678009341L;

	public static final int DEFAULT_FRAME_ID = 1;
	// XBee will not generate a TX Status Packet if this frame id sent
	public static final int NO_RESPONSE_FRAME_ID = 0;

	private int frameId;

	// TODO create XBeePacket(XBeeRequest) constructor and move operation there
	public XBeePacket getXBeePacket() {
        var frameData = this.getFrameData();

		if (frameData == null) {
			throw new IllegalArgumentException("frame data can't be null");
		}

		// TODO xbee packet should handle api/frame id
		return new XBeePacket(frameData);
	}

	public abstract int[] getFrameData();

	public abstract ApiId getApiId();

	public int getFrameId() {
		return frameId;
	}

    @Override
	public String toString() {
		return "apiId=" + this.getApiId() + ",frameId=" + this.getFrameId();
	}

	public void setFrameId(int frameId) {
		this.frameId = frameId;
	}

	// TODO clear method to reuse request
}
