package com.homeclimatecontrol.xbee.response.command;

import java.nio.ByteBuffer;

public class NIResponseReader extends CommandResponseReader {

    @Override
    public final CommandResponse read(ByteBuffer commandData) {

        var sb = new StringBuilder();

        while (commandData.hasRemaining()) {
            sb.append((char) commandData.get());
        }

        return new NIResponse(sb.toString());
    }
}
