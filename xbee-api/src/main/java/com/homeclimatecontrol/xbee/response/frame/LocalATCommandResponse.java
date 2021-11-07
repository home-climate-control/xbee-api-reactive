package com.homeclimatecontrol.xbee.response.frame;

import com.homeclimatecontrol.xbee.response.command.CommandResponse;
import com.homeclimatecontrol.xbee.util.HexFormat;
import com.rapplogic.xbee.api.AtCommand;

public class LocalATCommandResponse extends FrameIdAwareResponse {

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

    public final AtCommand.Command command;
    public final Status status;
    public final CommandResponse commandResponse;

    public LocalATCommandResponse(byte frameId, AtCommand.Command command, Status status, CommandResponse commandResponse) {
        super(frameId);
        this.command = command;
        this.status = status;
        this.commandResponse = commandResponse;
    }

    @Override
    public String toString() {
        return "{LocalATCommandResponse frameId=" + HexFormat.format(frameId)
                + ", command=" + command
                + ", status=" + status
                + ", response={" + commandResponse
                + "}}";
    }
}
