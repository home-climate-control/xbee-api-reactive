package com.homeclimatecontrol.xbee.response.command;

import com.homeclimatecontrol.xbee.util.HexFormat;
import com.rapplogic.xbee.api.AtCommand;

public abstract class PinConfigurationResponse extends CommandResponse {

    public final byte config;

    protected PinConfigurationResponse(AtCommand.Command command, byte config) {
        super(command);
        this.config = config;
    }

    @Override
    public String toString() {
        return command + "=" + HexFormat.format(config);
    }
}
