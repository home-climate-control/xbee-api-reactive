package com.homeclimatecontrol.xbee.response.command;

import com.rapplogic.xbee.api.AtCommand;

public class MYResponse extends CommandResponse {

    public final int address16;

    protected MYResponse(int address16) {
        super(AtCommand.Command.HV);
        this.address16 = address16;
    }

    @Override
    public String toString() {
        return "MY=" + String.format("0x%04X", address16);
    }
}
