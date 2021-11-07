package com.homeclimatecontrol.xbee.response.command;

import com.rapplogic.xbee.api.AtCommand;

/**
 * XBee command response, including the semantics.
 */
public abstract class CommandResponse {

    /**
     * The command that the response pertains to.
     */
    public final AtCommand.Command command;

    protected CommandResponse(AtCommand.Command command) {
        this.command = command;
    }
}
