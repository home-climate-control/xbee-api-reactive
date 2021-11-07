package com.homeclimatecontrol.xbee.util;

import java.nio.ByteBuffer;

/**
 * XBee checksum calculator.
 *
 * This object is NOT thread safe.
 *
 * Reference: <a href="https://www.digi.com/resources/documentation/Digidocs/90002002/Content/Tasks/t_calculate_checksum.htm">Calculate and verify checksums</a>
 *
 * @author Copyright &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2021
 */
public class XbeeChecksum {

    private int accumulator = 0;

    public void update(ByteBuffer buffer) {

        while (buffer.hasRemaining()) {
            accumulator += buffer.get();
        }
    }

    public byte getValue() {

        var checksum = (byte) (0xFF - (accumulator & 0xFF));

        // Additional verification

        var checksum2 = (accumulator + checksum) & 0xFF;

        if (checksum2!= 0xFF) {
            throw new IllegalArgumentException("Checksum verification failed, sum is 0x" + Integer.toHexString(checksum2));
        }

        return checksum;
    }
}
