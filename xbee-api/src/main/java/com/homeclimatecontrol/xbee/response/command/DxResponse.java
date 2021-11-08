package com.homeclimatecontrol.xbee.response.command;

import com.homeclimatecontrol.xbee.util.HexFormat;
import com.rapplogic.xbee.api.AtCommand;

/**
 * Generic class for Dx (x in 0....7 range) responses.
 *
 * These responses have subtle differences in the meaning of the response value, hence can't be treated the same.
 */
public class DxResponse<T> extends CommandResponse {

    public final byte code;
    public final T payload;

    public DxResponse(AtCommand.Command command, byte code, T payload) {
        super(command);
        this.code = code;
        this.payload = payload;
    }

    public String toString() {
        return "{" + command + " code=" + HexFormat.format(code) + ", payload=" + payload + "}";
    }
}
