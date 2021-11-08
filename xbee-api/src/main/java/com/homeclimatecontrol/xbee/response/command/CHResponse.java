package com.homeclimatecontrol.xbee.response.command;

import com.homeclimatecontrol.xbee.util.HexFormat;
import com.rapplogic.xbee.api.AtCommand;

public class CHResponse extends CommandResponse {

    public final byte channel;

    protected CHResponse(byte channel) {
        super(AtCommand.Command.CH);
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "CH=" + HexFormat.format(channel);
    }
}
