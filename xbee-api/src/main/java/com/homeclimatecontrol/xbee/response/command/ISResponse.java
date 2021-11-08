package com.homeclimatecontrol.xbee.response.command;

import com.rapplogic.xbee.api.AtCommand;

public class ISResponse extends CommandResponse {

    protected ISResponse() {
        super(AtCommand.Command.IS);
    }

    @Override
    public String toString() {
        return "IS";
    }
}
