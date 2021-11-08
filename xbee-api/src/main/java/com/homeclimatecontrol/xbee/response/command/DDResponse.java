package com.homeclimatecontrol.xbee.response.command;

import static com.rapplogic.xbee.api.AtCommand.Command.DD;

public class DDResponse extends CommandResponse {

    public final int deviceTypeIdentifier;

    public DDResponse(int deviceTypeIdentifier) {
        super(DD);
        this.deviceTypeIdentifier = deviceTypeIdentifier;
    }

    @Override
    public String toString() {
        return String.format("{DD=0x%04X}", deviceTypeIdentifier);
    }
}
