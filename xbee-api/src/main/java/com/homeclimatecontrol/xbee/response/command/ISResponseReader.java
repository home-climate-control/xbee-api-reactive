package com.homeclimatecontrol.xbee.response.command;

import java.nio.ByteBuffer;

public class ISResponseReader extends CommandResponseReader {

    @Override
    public CommandResponse read(ByteBuffer commandData) {
        // No payload
        return new ISResponse();
    }
}
