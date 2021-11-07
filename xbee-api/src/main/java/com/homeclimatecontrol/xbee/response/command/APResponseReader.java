package com.homeclimatecontrol.xbee.response.command;

import java.nio.ByteBuffer;

public class APResponseReader extends CommandResponseReader {

    @Override
    public CommandResponse read(ByteBuffer commandData) {
        // AP command requires no separate response, it is contained in the status
        return null;
    }
}
