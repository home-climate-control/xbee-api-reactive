package com.homeclimatecontrol.xbee.response.command;

/**
 * AP command response reader.
 *
 * @author Copyright &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2021
 */
public class APResponseReader extends SingleByteResponseReader {

    @Override
    protected CommandResponse create(byte payload) {
        return new APResponse(payload);
    }
}
