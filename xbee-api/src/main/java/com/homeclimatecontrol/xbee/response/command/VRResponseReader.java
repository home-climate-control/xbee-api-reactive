package com.homeclimatecontrol.xbee.response.command;

public class VRResponseReader extends DoubleByteResponseReader {

    @Override
    protected CommandResponse create(int payload) {
        return new VRResponse(payload);
    }
}
