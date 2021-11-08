package com.homeclimatecontrol.xbee.response.command;

public class MYResponseReader extends DoubleByteResponseReader {

    @Override
    protected CommandResponse create(int payload) {
        return new MYResponse(payload);
    }
}
