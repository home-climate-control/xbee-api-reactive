package com.homeclimatecontrol.xbee.response.command;

import java.nio.ByteBuffer;

public class HVResponseReader extends CommandResponseReader {

    @Override
    public CommandResponse read(ByteBuffer commandData) {
        return new HVResponse((commandData.get() << 8) + (commandData.get() & 0xFF));
    }
}
