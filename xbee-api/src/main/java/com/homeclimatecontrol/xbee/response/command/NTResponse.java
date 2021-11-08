package com.homeclimatecontrol.xbee.response.command;

import com.homeclimatecontrol.xbee.util.HexFormat;
import com.rapplogic.xbee.api.AtCommand;

import java.time.Duration;

public class NTResponse extends CommandResponse {

    public final byte unknown;
    public final byte timeout;

    protected NTResponse(byte unknown, byte timeout) {
        super(AtCommand.Command.NT);
        this.unknown = unknown;
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return "{NT unknown=" + HexFormat.format(unknown) + ", timeout=" + HexFormat.format(timeout) + " (" + Duration.ofMillis(timeout * 100) + ")}";
    }
}
