package com.homeclimatecontrol.xbee.response.command;

public class HVResponseReader extends DoubleByteResponseReader {

    @Override
    protected CommandResponse create(int payload) {
        return new HVResponse(payload);
    }
}
