package com.homeclimatecontrol.xbee.response.frame;

import com.homeclimatecontrol.xbee.response.command.APResponseReader;
import com.homeclimatecontrol.xbee.response.command.CommandResponseReader;
import com.homeclimatecontrol.xbee.response.command.HVResponseReader;
import com.homeclimatecontrol.xbee.response.command.NDResponseReader;
import com.rapplogic.xbee.api.AtCommand;

import java.nio.ByteBuffer;
import java.util.Map;

import static com.rapplogic.xbee.api.AtCommand.Command.AP;
import static com.rapplogic.xbee.api.AtCommand.Command.HV;
import static com.rapplogic.xbee.api.AtCommand.Command.ND;

public abstract class FrameReader {

    private static Map<AtCommand.Command, CommandResponseReader> command2reader = Map.of(
            AP, new APResponseReader(),
            HV, new HVResponseReader(),
            ND, new NDResponseReader()
    );

    protected CommandResponseReader getReader(AtCommand.Command command) {
        var result = command2reader.get(command);

        if (result == null) {
            throw new IllegalArgumentException("No command response reader exists for command=" + command);
        }

        return result;
    }

    /**
     * Read the frame and return the corresponding data structure.
     *
     * @param frameData XBee frame data, starting at offset 3 (after Frame Type), not including the checksum.
     *
     * @return Frame object.
     */
    public abstract XBeeResponseFrame read(ByteBuffer frameData);
}
