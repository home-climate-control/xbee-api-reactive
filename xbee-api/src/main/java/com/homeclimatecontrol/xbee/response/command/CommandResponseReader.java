package com.homeclimatecontrol.xbee.response.command;

import java.nio.ByteBuffer;

public abstract class CommandResponseReader {

    public abstract CommandResponse read(ByteBuffer commandData);
}
