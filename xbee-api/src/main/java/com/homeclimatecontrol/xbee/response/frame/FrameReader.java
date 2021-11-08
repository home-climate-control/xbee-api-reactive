package com.homeclimatecontrol.xbee.response.frame;

import com.homeclimatecontrol.xbee.response.command.APResponseReader;
import com.homeclimatecontrol.xbee.response.command.CHResponseReader;
import com.homeclimatecontrol.xbee.response.command.CommandResponseReader;
import com.homeclimatecontrol.xbee.response.command.D0ResponseReader;
import com.homeclimatecontrol.xbee.response.command.D1ResponseReader;
import com.homeclimatecontrol.xbee.response.command.D2ResponseReader;
import com.homeclimatecontrol.xbee.response.command.D3ResponseReader;
import com.homeclimatecontrol.xbee.response.command.D4ResponseReader;
import com.homeclimatecontrol.xbee.response.command.D5ResponseReader;
import com.homeclimatecontrol.xbee.response.command.D6ResponseReader;
import com.homeclimatecontrol.xbee.response.command.D7ResponseReader;
import com.homeclimatecontrol.xbee.response.command.DDResponseReader;
import com.homeclimatecontrol.xbee.response.command.HVResponseReader;
import com.homeclimatecontrol.xbee.response.command.ISResponseReader;
import com.homeclimatecontrol.xbee.response.command.MYResponseReader;
import com.homeclimatecontrol.xbee.response.command.NCResponseReader;
import com.homeclimatecontrol.xbee.response.command.NDResponseReader;
import com.homeclimatecontrol.xbee.response.command.NIResponseReader;
import com.homeclimatecontrol.xbee.response.command.NTResponseReader;
import com.homeclimatecontrol.xbee.response.command.P0ResponseReader;
import com.homeclimatecontrol.xbee.response.command.VRResponseReader;
import com.rapplogic.xbee.api.AtCommand;

import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.Map;

import static com.rapplogic.xbee.api.AtCommand.Command.AP;
import static com.rapplogic.xbee.api.AtCommand.Command.CH;
import static com.rapplogic.xbee.api.AtCommand.Command.D0;
import static com.rapplogic.xbee.api.AtCommand.Command.D1;
import static com.rapplogic.xbee.api.AtCommand.Command.D2;
import static com.rapplogic.xbee.api.AtCommand.Command.D3;
import static com.rapplogic.xbee.api.AtCommand.Command.D4;
import static com.rapplogic.xbee.api.AtCommand.Command.D5;
import static com.rapplogic.xbee.api.AtCommand.Command.D6;
import static com.rapplogic.xbee.api.AtCommand.Command.D7;
import static com.rapplogic.xbee.api.AtCommand.Command.DD;
import static com.rapplogic.xbee.api.AtCommand.Command.HV;
import static com.rapplogic.xbee.api.AtCommand.Command.IS;
import static com.rapplogic.xbee.api.AtCommand.Command.MY;
import static com.rapplogic.xbee.api.AtCommand.Command.NC;
import static com.rapplogic.xbee.api.AtCommand.Command.ND;
import static com.rapplogic.xbee.api.AtCommand.Command.NI;
import static com.rapplogic.xbee.api.AtCommand.Command.NT;
import static com.rapplogic.xbee.api.AtCommand.Command.P0;
import static com.rapplogic.xbee.api.AtCommand.Command.VR;

public abstract class FrameReader {

    private static final Map<AtCommand.Command, CommandResponseReader> command2reader = Map.ofEntries(
            new AbstractMap.SimpleEntry<>(AP, new APResponseReader()),

            // These are similar, but different
            new AbstractMap.SimpleEntry<>(D0, new D0ResponseReader()),
            new AbstractMap.SimpleEntry<>(D1, new D1ResponseReader()),
            new AbstractMap.SimpleEntry<>(D2, new D2ResponseReader()),
            new AbstractMap.SimpleEntry<>(D3, new D3ResponseReader()),
            new AbstractMap.SimpleEntry<>(D4, new D4ResponseReader()),
            new AbstractMap.SimpleEntry<>(D5, new D5ResponseReader()),
            new AbstractMap.SimpleEntry<>(D6, new D6ResponseReader()),
            new AbstractMap.SimpleEntry<>(D7, new D7ResponseReader()),

            new AbstractMap.SimpleEntry<>(DD, new DDResponseReader()),
            new AbstractMap.SimpleEntry<>(CH, new CHResponseReader()),
            new AbstractMap.SimpleEntry<>(HV, new HVResponseReader()),
            new AbstractMap.SimpleEntry<>(IS, new ISResponseReader()),
            new AbstractMap.SimpleEntry<>(MY, new MYResponseReader()),
            new AbstractMap.SimpleEntry<>(NC, new NCResponseReader()),
            new AbstractMap.SimpleEntry<>(ND, new NDResponseReader()),
            new AbstractMap.SimpleEntry<>(NI, new NIResponseReader()),
            new AbstractMap.SimpleEntry<>(NT, new NTResponseReader()),

            new AbstractMap.SimpleEntry<>(P0, new P0ResponseReader()),
            new AbstractMap.SimpleEntry<>(VR, new VRResponseReader())
    );

    protected CommandResponseReader getReader(AtCommand.Command command) {
        var result = command2reader.get(command);

        if (result == null) {
            throw new UnsupportedOperationException("No command response reader exists for command=" + command);
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
