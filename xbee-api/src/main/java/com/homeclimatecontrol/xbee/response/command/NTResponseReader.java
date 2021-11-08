package com.homeclimatecontrol.xbee.response.command;

import java.nio.ByteBuffer;

public class NTResponseReader extends CommandResponseReader {

    @Override
    public CommandResponse read(ByteBuffer commandData) {

        // First byte - content unknown
        var unknown = commandData.get();
        var timeout = commandData.get();

        return new NTResponse(unknown, timeout);
    }
}
