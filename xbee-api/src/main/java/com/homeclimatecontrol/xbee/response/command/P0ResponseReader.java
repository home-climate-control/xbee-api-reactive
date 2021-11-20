package com.homeclimatecontrol.xbee.response.command;

public class P0ResponseReader extends SingleByteResponseReader {

    @Override
    protected CommandResponse create(byte payload) {
        return new P0Response(payload);
    }
}
