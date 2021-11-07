package com.homeclimatecontrol.xbee.response.frame;

import java.nio.ByteBuffer;

public abstract class FrameIdAwareReader extends FrameReader {

    @Override
    public XBeeResponseFrame read(ByteBuffer frameData) {
        byte frameId = frameData.get();
        return read(frameId, frameData.slice());
    }

    protected abstract XBeeResponseFrame read(byte frameId, ByteBuffer slice);
}
