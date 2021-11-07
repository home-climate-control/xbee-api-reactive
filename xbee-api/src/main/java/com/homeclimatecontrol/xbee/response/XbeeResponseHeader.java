package com.homeclimatecontrol.xbee.response;

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;

public class XbeeResponseHeader {

    public final ApiId frameType;
    public final byte frameId;
    public final XBeeAddress64 address64;
    public final XBeeAddress16 address16;
    public final byte broadcastRadius;
    public final byte options;

    public XbeeResponseHeader(ApiId frameType, byte frameId, XBeeAddress64 address64, XBeeAddress16 address16, byte broadcastRadius, byte options) {
        this.frameType = frameType;
        this.frameId = frameId;
        this.address64 = address64;
        this.address16 = address16;
        this.broadcastRadius = broadcastRadius;
        this.options = options;
    }
}
