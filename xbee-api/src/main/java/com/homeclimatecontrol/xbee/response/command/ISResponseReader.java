package com.homeclimatecontrol.xbee.response.command;

import java.nio.ByteBuffer;

/**
 * IS command response reader.
 *
 * @author Copyright &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2021
 */
public class ISResponseReader extends CommandResponseReader {

    @Override
    public CommandResponse read(ByteBuffer commandData) {
        // AP command requires no separate response, it is contained in the status
        return null;
    }
}
