package com.homeclimatecontrol.xbee.response.command;

import com.rapplogic.xbee.api.AtCommand;

public class P0Response extends PinConfigurationResponse {

    protected P0Response(byte config) {
        super(AtCommand.Command.P0, config);
    }
}
