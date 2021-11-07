package com.homeclimatecontrol.xbee.response.frame;

import com.rapplogic.xbee.api.AtCommand;

import java.nio.ByteBuffer;

public class LocalATCommandResponseReader extends FrameIdAwareReader {

    @Override
    public XBeeResponseFrame read(byte frameId, ByteBuffer frameData) {
        var commandName = Character.toString((char) frameData.get()) + Character.toString((char) frameData.get());
        var command = AtCommand.Command.valueOf(commandName);
        var status = LocalATCommandResponse.Status.valueOf(frameData.get());
        var commandResponse = getReader(command).read(frameData.slice());

        return new LocalATCommandResponse(frameId, command, status, commandResponse);
    }
}
