package com.homeclimatecontrol.xbee.response.command;

import java.nio.ByteBuffer;

public abstract class SingleByteResponseReader extends CommandResponseReader {

    @Override
    public final CommandResponse read(ByteBuffer commandData) {
        return create(commandData.get());
    }

    protected abstract CommandResponse create(byte payload);
}
