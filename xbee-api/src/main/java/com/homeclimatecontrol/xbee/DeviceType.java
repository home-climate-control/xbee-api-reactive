package com.homeclimatecontrol.xbee;

import com.homeclimatecontrol.xbee.util.HexFormat;

public enum DeviceType {
    COORDINATOR( (byte) 0),
    ROUTER((byte) 1),
    END_DEVICE((byte) 2);

    public final byte type;

    DeviceType(byte type) {
        this.type = type;
    }

    public static DeviceType valueOf(byte type) {
        for (var t : values()) {
            if (t.type == type) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown type " + HexFormat.format(type));
    }
}
