package com.homeclimatecontrol.xbee.response.frame;

import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;

import java.nio.ByteBuffer;

public class RemoteATCommandResponseReader extends FrameIdAwareReader {

    @Override
    protected XBeeResponseFrame read(byte frameId, ByteBuffer frameData) {

        var address64 = new XBeeAddress64(frameData);
        var address16 = new XBeeAddress16(frameData);
        var commandName = Character.toString((char) frameData.get()) + Character.toString((char) frameData.get());
        var command = AtCommand.Command.valueOf(commandName);
        var status = RemoteATCommandResponse.Status.valueOf(frameData.get());

        // Response may be unavailable if the command was to set  the value, not read it
        var commandResponse = frameData.hasRemaining() ? getReader(command).read(frameData.slice()) : null;

        return new RemoteATCommandResponse(frameId, address64, address16, command, status, commandResponse);
    }
}
