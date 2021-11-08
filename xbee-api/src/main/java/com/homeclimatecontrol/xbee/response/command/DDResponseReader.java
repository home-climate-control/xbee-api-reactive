package com.homeclimatecontrol.xbee.response.command;

public class DDResponseReader extends QuadByteResponseReader {

    @Override
    protected CommandResponse create(int payload) {
        return new DDResponse(payload);
    }
}
