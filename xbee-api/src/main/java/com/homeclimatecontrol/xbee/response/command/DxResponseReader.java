package com.homeclimatecontrol.xbee.response.command;

import com.rapplogic.xbee.api.AtCommand;

import java.nio.ByteBuffer;

public class DxResponseReader extends CommandResponseReader {

    @Override
    public CommandResponse read(ByteBuffer commandData) {
        return new DxResponse<>(resolveCommand(), commandData.get(), null);
    }

    private AtCommand.Command resolveCommand() {
        var offset = getClass().getSimpleName().charAt(1);
        return AtCommand.Command.valueOf("D" + offset);
    }
}
