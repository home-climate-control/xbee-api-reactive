package com.homeclimatecontrol.xbee.util;

import java.nio.ByteBuffer;

public class HexFormat {

    private HexFormat() {}

    public static String format(byte value) {
        return String.format("0x%02X", value & 0xFF);
    }

    public static String format(int value) {
        return String.format("0x%02X", value);
    }

    public static String format(ByteBuffer data) {

        var sb = new StringBuilder();

        while (true) {

            sb.append(HexFormat.format(data.get()));

            if (data.hasRemaining()) {
                sb.append(", ");
            } else {
                return sb.toString();
            }
        }
    }
}
