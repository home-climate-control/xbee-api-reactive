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

import com.rapplogic.xbee.util.ByteUtils;

/**
 * The super class of all XBee transmit packets.
 * Constructs frame data portion of an XBee packet
 * <p/>
 * @author andrew
 *
 */
public abstract class XBeeRequest {

    /**
     * No response frame ID.
     *
     * XBee will not generate a TX Status Packet if this frame ID is sent.
     */
    public static final byte NO_RESPONSE_FRAME_ID = 0;

    public static final byte DEFAULT_FRAME_ID = 1;

    private final byte frameId;

    protected XBeeRequest(byte frameId) {
        this.frameId = frameId;
    }

    // TODO create XBeePacket(XBeeRequest) constructor and move operation there
    public XBeePacket getXBeePacket() {
        var frameData = getFrameData();

        if (frameData == null) {
            throw new IllegalArgumentException("frame data can't be null");
        }

        // TODO xbee packet should handle api/frame id
        return new XBeePacket(frameData);
    }

    public abstract int[] getFrameData();

    public abstract ApiId getApiId();

    public byte getFrameId() {
        return frameId;
    }

    @Override
    public String toString() {
        return "apiId=" + getApiId() + ",frameId=" + ByteUtils.toBase16(getFrameId());
    }
}
