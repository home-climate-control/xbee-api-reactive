package com.homeclimatecontrol.xbee.response.command;

import com.homeclimatecontrol.xbee.AddressParser;
import com.homeclimatecontrol.xbee.DeviceType;
import com.homeclimatecontrol.xbee.response.frame.LocalATCommandResponse;
import com.homeclimatecontrol.xbee.util.HexFormat;
import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;

import java.nio.ByteBuffer;

public class NDResponse extends CommandResponse {

    public final XBeeAddress16 address16;
    public final XBeeAddress64 address64;
    public final String nodeIdentifier;
    public final XBeeAddress16 parentAddress;
    public final DeviceType deviceType;
    public final LocalATCommandResponse.Status status;
    public final int[] profileId;
    public final int[] mfgId;

    public NDResponse(
            XBeeAddress16 address16,
            XBeeAddress64 address64,
            String nodeIdentifier,
            XBeeAddress16 parentAddress,
            DeviceType deviceType,
            LocalATCommandResponse.Status status,
            int[] profileId,
            int[] mfgId) {
        super(AtCommand.Command.ND);

        this.address16 = address16;
        this.address64 = address64;
        this.nodeIdentifier = nodeIdentifier;
        this.parentAddress = parentAddress;
        this.deviceType = deviceType;
        this.status = status;
        this.profileId = profileId;
        this.mfgId = mfgId;
    }

    @Override
    public String toString() {
        return "{ND address=" + AddressParser.render4x4(address64) + "/" + address16
                + ", NI=" + nodeIdentifier
                + ", parent=" + parentAddress
                + ", deviceType=" + deviceType
                + ", status=" + status
                + ", profileId=" + HexFormat.format(ByteBuffer.wrap(new byte[] {(byte) profileId[0], (byte) profileId[1]}))
                + ", mfgId=" + HexFormat.format(ByteBuffer.wrap(new byte[] {(byte) mfgId[0], (byte) mfgId[1]}))
                + "}";
    }
}
