package com.homeclimatecontrol.xbee.response.command;

public class CHResponseReader extends SingleByteResponseReader {

    @Override
    protected CommandResponse create(byte payload) {
        return new CHResponse(payload);
    }
}
