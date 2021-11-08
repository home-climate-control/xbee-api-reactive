package com.homeclimatecontrol.xbee.response.command;

public class NCResponseReader extends SingleByteResponseReader {

    @Override
    protected CommandResponse create(byte payload) {
        return new NCResponse(payload);
    }
}
