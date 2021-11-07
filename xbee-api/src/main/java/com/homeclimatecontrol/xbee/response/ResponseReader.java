package com.homeclimatecontrol.xbee.response;

import com.homeclimatecontrol.xbee.util.XbeeChecksum;
import com.rapplogic.xbee.api.XBeeResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ResponseReader {

    private final Logger logger = LogManager.getLogger();

    /**
     * Read the XBee response from the input stream.
     *
     * @param in Stream to read from. The stream pointer is expected to be at offset 1, right after the {@code 0x7E}
     *           start delimiter.
     * @return A newly instantiated response object.
     */
    public XBeeResponse read(InputStream in) throws IOException {

        // Read the packet in two blocks:
        // - Fixed length header
        // - Variable length payload (including the checksum)

        var headerBuffer = in.readNBytes(2);

        verifyRead(2, headerBuffer.length, "length");

        var frameSize = readLength(headerBuffer);
        var frameBuffer = in.readNBytes(frameSize + 1);
        var frameData = ByteBuffer.wrap(frameBuffer, 0, frameBuffer.length - 1);

        verifyRead(frameSize + 1, frameBuffer.length, "payload");
        verifyChecksum(
                frameData,
                frameBuffer[frameSize]);

        return null;
    }

    private void verifyChecksum(ByteBuffer payload, byte checksumExpected) {
        var checksum = new XbeeChecksum();

        checksum.update(payload);

        var checksumActual = (byte) checksum.getValue();

        if (checksumActual != checksumExpected) {
            throw new IllegalArgumentException("Checksum mismatch, expected 0x" + Integer.toHexString(checksumExpected & 0xFF) + ", actual 0x0" + Long.toHexString(checksumActual));
        }
    }

    private int readLength(byte[] buffer) {
        return (buffer[0] << 8) + (buffer[1] & 0xFF);
    }

    private void verifyRead(int expected, int actual, String type) throws IOException {
        if (expected != actual) {
            throw new IOException("Read " + actual + " " + type + " bytes instead of " + expected + " expected");
        }
    }
}
