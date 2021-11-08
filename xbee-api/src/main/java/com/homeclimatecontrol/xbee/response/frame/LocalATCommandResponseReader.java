package com.homeclimatecontrol.xbee.response.frame;

import com.rapplogic.xbee.api.AtCommand;

import java.nio.ByteBuffer;

/**
 * Local AT command response reader.
 *
 * See <a href="https://www.digi.com/resources/documentation/Digidocs/90002002/Default.htm#Reference/r_frame_0x88.htm?">Local AT Command Response</a>.
 *
 * @author Copyright &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2021
 */
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
