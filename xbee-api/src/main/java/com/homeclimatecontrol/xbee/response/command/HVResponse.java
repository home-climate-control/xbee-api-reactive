package com.homeclimatecontrol.xbee.response.command;

import com.homeclimatecontrol.xbee.util.HexFormat;
import com.rapplogic.xbee.api.AtCommand;

public class HVResponse extends CommandResponse {

    public final int hardwareVersion;

    protected HVResponse(int hardwareVersion) {
        super(AtCommand.Command.HV);
        this.hardwareVersion = hardwareVersion;
    }

    @Override
    public String toString() {
        return "HV=" + HexFormat.format(hardwareVersion);
    }
}
