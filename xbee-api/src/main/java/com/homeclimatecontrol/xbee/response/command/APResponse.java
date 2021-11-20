package com.homeclimatecontrol.xbee.response.command;

import com.homeclimatecontrol.xbee.util.HexFormat;
import com.rapplogic.xbee.api.AtCommand;

public class APResponse extends CommandResponse {

    public final byte mode;

    protected APResponse(byte mode) {
        super(AtCommand.Command.AP);
        this.mode = mode;
    }

    @Override
    public String toString() {
        return "AP=" + HexFormat.format(mode);
    }
}
