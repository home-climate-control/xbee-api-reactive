package com.homeclimatecontrol.xbee.response.command;

import java.nio.ByteBuffer;

public abstract class QuadByteResponseReader extends CommandResponseReader {

    @Override
    public final CommandResponse read(ByteBuffer commandData) {

        var payload = 0;

        for (var offset = 0; offset < 4; offset++) {
            payload <<= 8;
            payload += commandData.get() & 0xFF;
        }

        return create(payload);
    }

    protected abstract CommandResponse create(int payload);
}
