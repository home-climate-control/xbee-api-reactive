package com.homeclimatecontrol.xbee.response.frame;

public class FrameIdAwareResponse extends XBeeResponseFrame {
    public final byte frameId;

    public FrameIdAwareResponse(byte frameId) {
        this.frameId = frameId;
    }
}
