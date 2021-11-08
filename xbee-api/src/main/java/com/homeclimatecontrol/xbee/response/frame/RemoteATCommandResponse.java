package com.homeclimatecontrol.xbee.response.frame;

import com.homeclimatecontrol.xbee.AddressParser;
import com.homeclimatecontrol.xbee.response.command.CommandResponse;
import com.homeclimatecontrol.xbee.util.HexFormat;
import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;

public class RemoteATCommandResponse extends ATCommandResponse<RemoteATCommandResponse.Status> {

    public enum Status {

        OK((byte) 0x00),
        ERROR((byte) 0x01),
        INVALID_COMMAND((byte) 0x02),
        INVALID_PARAMETER((byte) 0x03),
        TRANSMISSION_FAILURE((byte) 0x04),
        NO_SECURE_SESSION((byte) 0x0B),
        ENCRYPTION_ERROR((byte) 0x0C),
        SENT_INSECURELY((byte) 0x0D);

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

    public final XBeeAddress64 address64;
    public final XBeeAddress16 address16;

    public RemoteATCommandResponse(byte frameId, XBeeAddress64 address64, XBeeAddress16 address16, AtCommand.Command command, Status status, CommandResponse commandResponse) {
        super(frameId, command, status, commandResponse);
        this.address64 = address64;
        this.address16 = address16;
    }

    @Override
    public String toString() {
        return "{" + getClass().getSimpleName() + " frameId=" + HexFormat.format(frameId)
                + ", address" + AddressParser.render4x4(address64) + "/" + address16
                + ", command=" + command
                + ", status=" + status
                + ", response=" + commandResponse
                + "}";
    }
}
