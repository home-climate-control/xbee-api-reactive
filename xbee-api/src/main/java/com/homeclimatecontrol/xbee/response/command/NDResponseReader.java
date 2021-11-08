package com.homeclimatecontrol.xbee.response.command;

import com.homeclimatecontrol.xbee.DeviceType;
import com.homeclimatecontrol.xbee.response.frame.LocalATCommandResponse;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;

import java.nio.ByteBuffer;

public class NDResponseReader extends CommandResponseReader {

    @Override
    public CommandResponse read(ByteBuffer commandData) {

        var address16 = new XBeeAddress16(new int[] { commandData.get() & 0xFF, commandData.get() & 0xFF });
        var address64 = new XBeeAddress64(new int[] { commandData.get() & 0xFF, commandData.get() & 0xFF, commandData.get() & 0xFF, commandData.get() & 0xFF, commandData.get() & 0xFF, commandData.get() & 0xFF, commandData.get() & 0xFF, commandData.get() & 0xFF, });
        var nodeIdentifier = readNI(commandData);
        var parentAddress = new XBeeAddress16(new int[] { commandData.get() & 0xFF, commandData.get() & 0xFF });
        var deviceType = DeviceType.valueOf(commandData.get());
        var status = LocalATCommandResponse.Status.valueOf(commandData.get());
        var profileId = new int[] { commandData.get() & 0xFF, commandData.get() & 0xFF };
        var mfgId = new int[] { commandData.get() & 0xFF, commandData.get() & 0xFF };

        return new NDResponse(
                address16,
                address64,
                nodeIdentifier,
                parentAddress,
                deviceType,
                status,
                profileId,
                mfgId
        );
    }

    private String readNI(ByteBuffer source) {
        var sb = new StringBuilder();

        while (true) {

            var c = source.get();

            if (c == 0) {
                return sb.toString();
            }

            // VT: NOTE: The symbols are supposed to be ASCII only (32...126), but the hell with it for now
            sb.append((char) c);
        }
    }
}
