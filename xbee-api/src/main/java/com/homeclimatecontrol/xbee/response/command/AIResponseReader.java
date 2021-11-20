package com.homeclimatecontrol.xbee.response.command;

public class AIResponseReader extends SingleByteResponseReader {

    @Override
    protected CommandResponse create(byte payload) {
        return new AIResponse(payload);
    }
}
