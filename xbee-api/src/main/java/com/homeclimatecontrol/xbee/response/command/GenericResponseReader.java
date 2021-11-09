package com.homeclimatecontrol.xbee.response.command;

import com.rapplogic.xbee.api.AtCommand;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class GenericResponseReader extends CommandResponseReader {

    public final AtCommand.Command command;

    public GenericResponseReader(AtCommand.Command command) {
        this.command = command;
    }

    @Override
    public CommandResponse read(ByteBuffer commandData) {

        var buffer = new ByteArrayOutputStream();

        while (commandData.hasRemaining()) {
            buffer.write(commandData.get());
        }

        return new GenericResponse(command, buffer.toByteArray());
    }
}
