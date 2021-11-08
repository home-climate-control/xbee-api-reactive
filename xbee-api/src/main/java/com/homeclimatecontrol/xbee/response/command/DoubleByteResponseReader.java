package com.homeclimatecontrol.xbee.response.command;

import java.nio.ByteBuffer;

public abstract class DoubleByteResponseReader extends CommandResponseReader {

    @Override
    public final CommandResponse read(ByteBuffer commandData) {
        var payload = (commandData.get() << 8) + (commandData.get() & 0xFF);
        return create(payload);
    }

    protected abstract CommandResponse create(int payload);
}
