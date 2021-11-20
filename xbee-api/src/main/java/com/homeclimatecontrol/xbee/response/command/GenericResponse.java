package com.homeclimatecontrol.xbee.response.command;

import com.homeclimatecontrol.xbee.util.HexFormat;
import com.rapplogic.xbee.api.AtCommand;

import java.nio.ByteBuffer;

public class GenericResponse extends CommandResponse {

    public final byte[] data;

    protected GenericResponse(AtCommand.Command command, byte[] data) {
        super(command);
        this.data = data;
    }

    @Override
    public String toString() {
        return "{" + command + " (generic) data=" + HexFormat.format(ByteBuffer.wrap(data)) + "}";
    }
}
