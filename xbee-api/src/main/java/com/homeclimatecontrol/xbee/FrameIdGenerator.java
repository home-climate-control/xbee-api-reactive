package com.homeclimatecontrol.xbee;

import com.rapplogic.xbee.api.XBeeRequest;

/**
 * A singleton to generate a packet frame ID.
 */
public class FrameIdGenerator {

    private static final byte MAX_ID = (byte) 0xFF;

    private byte currentId = MAX_ID;

    private static final FrameIdGenerator instance = new FrameIdGenerator();

    public static FrameIdGenerator getInstance() {
        return instance;
    }

    public synchronized void reset() {
        currentId = MAX_ID;
    }

    public synchronized byte getNext() {

        if (currentId == MAX_ID) {
            // Can't be 0 or we won't get the response
            // Also, skip the tainted DEFAULT_FRAME_ID to avoid the warning
            currentId = XBeeRequest.DEFAULT_FRAME_ID;
        }

        return ++currentId;
    }
}
