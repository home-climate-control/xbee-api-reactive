package com.homeclimatecontrol.xbee.response.command;

import com.rapplogic.xbee.api.AtCommand;

public class NIResponse extends CommandResponse {

    public final String nodeIdentifier;

    protected NIResponse(String nodeIdentifier) {
        super(AtCommand.Command.NI);
        this.nodeIdentifier = nodeIdentifier;
    }

    @Override
    public String toString() {
        return "{NI=" + nodeIdentifier + "}";
    }
}
