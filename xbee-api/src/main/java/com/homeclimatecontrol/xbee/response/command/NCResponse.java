package com.homeclimatecontrol.xbee.response.command;

import com.rapplogic.xbee.api.AtCommand;

public class NCResponse extends CommandResponse {

    public final byte remainingChildren;

    protected NCResponse(byte remainingChildren) {
        super(AtCommand.Command.NC);
        this.remainingChildren = remainingChildren;
    }

    @Override
    public String toString() {
        return "NC=" + remainingChildren;
    }
}
