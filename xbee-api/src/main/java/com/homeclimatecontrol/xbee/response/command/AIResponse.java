package com.homeclimatecontrol.xbee.response.command;

import com.homeclimatecontrol.xbee.util.HexFormat;
import com.rapplogic.xbee.api.AtCommand;

public class AIResponse extends CommandResponse {

    public final byte status;

    protected AIResponse(byte status) {
        super(AtCommand.Command.AI);
        this.status = status;
    }

    @Override
    public String toString() {
        return "CH=" + HexFormat.format(status);
    }
}
