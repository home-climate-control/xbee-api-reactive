package com.homeclimatecontrol.xbee.response.frame;

import com.homeclimatecontrol.xbee.response.command.CommandResponse;
import com.rapplogic.xbee.api.AtCommand;

public abstract class ATCommandResponse<T> extends FrameIdAwareResponse {

    public final AtCommand.Command command;
    public final T status;
    public final CommandResponse commandResponse;

    protected ATCommandResponse(byte frameId, AtCommand.Command command, T status, CommandResponse commandResponse) {
        super(frameId);
        this.command = command;
        this.status = status;
        this.commandResponse = commandResponse;
    }
}
