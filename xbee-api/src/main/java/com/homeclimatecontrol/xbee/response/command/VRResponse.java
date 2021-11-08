package com.homeclimatecontrol.xbee.response.command;

import com.rapplogic.xbee.api.AtCommand;

public class VRResponse extends CommandResponse {

    public final int hardwareVersion;

    protected VRResponse(int address16) {
        super(AtCommand.Command.VR);
        hardwareVersion = address16;
    }

    @Override
    public String toString() {
        return command + "=" + String.format("0x%04X", hardwareVersion);
    }
}
