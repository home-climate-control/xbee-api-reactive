package com.homeclimatecontrol.xbee.response.frame;

import com.homeclimatecontrol.xbee.response.command.CommandResponse;
import com.homeclimatecontrol.xbee.util.HexFormat;
import com.rapplogic.xbee.api.AtCommand;

public class LocalATCommandResponse extends ATCommandResponse<LocalATCommandResponse.Status> {

    public enum Status {

        OK((byte) 0x00),
        ERROR((byte) 0x01),
        INVALID_COMMAND((byte) 0x02),
        INVALID_PARAMETER((byte) 0x03);

        public final byte code;

        Status(byte code) {
            this.code = code;
        }

        public static Status valueOf(byte code) {
            for (var status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown status " + HexFormat.format(code));
        }
    }

    public LocalATCommandResponse(byte frameId, AtCommand.Command command, Status status, CommandResponse commandResponse) {
        super(frameId, command, status, commandResponse);
    }

    @Override
    public String toString() {
        return "{" + getClass().getSimpleName() + " frameId=" + HexFormat.format(frameId)
                + ", command=" + command
                + ", status=" + status
                + ", response=" + commandResponse
                + "}";
    }
}
