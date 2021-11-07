package com.homeclimatecontrol.xbee.response;

import com.homeclimatecontrol.xbee.FrameType;
import com.homeclimatecontrol.xbee.response.frame.FrameReader;
import com.homeclimatecontrol.xbee.response.frame.LocalATCommandResponseReader;
import com.homeclimatecontrol.xbee.util.HexFormat;
import com.homeclimatecontrol.xbee.util.XbeeChecksum;
import com.rapplogic.xbee.api.XBeeResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;

import static com.homeclimatecontrol.xbee.FrameType.LOCAL_AT_COMMAND_RESPONSE;

public class ResponseReader {

    private final Logger logger = LogManager.getLogger();

    private static Map<FrameType, FrameReader> frame2reader = Map.of(
            LOCAL_AT_COMMAND_RESPONSE, new LocalATCommandResponseReader()
    );

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

        verifyRead(frameSize + 1, frameBuffer.length, "payload");
        verifyChecksum(frameBuffer);

        var frame = getReader(frameBuffer[0]).read(ByteBuffer.wrap(frameBuffer, 1, frameBuffer.length - 2));

        logger.info("read: {}", frame);

        return null;
    }

    private int readLength(byte[] buffer) {
        return (buffer[0] << 8) + (buffer[1] & 0xFF);
    }

    private void verifyRead(int expected, int actual, String type) throws IOException {
        if (expected != actual) {
            throw new IOException("Read " + actual + " " + type + " bytes instead of " + expected + " expected");
        }
    }

    void verifyChecksum(byte[] frameBuffer) {

        var payload = ByteBuffer.wrap(frameBuffer, 0, frameBuffer.length - 1);
        var checksumExpected = frameBuffer[frameBuffer.length - 1];
        var checksum = new XbeeChecksum();

        checksum.update(payload);

        var checksumActual = checksum.getValue();

        if (checksumActual != checksumExpected) {
            throw new IllegalArgumentException("Checksum mismatch, expected " + HexFormat.format(checksumExpected) + ", actual " + HexFormat.format(checksumActual));
        }
    }

    private FrameReader getReader(byte type) {

        var frameType = FrameType.getByType(type);
        var result = frame2reader.get(frameType);

        if (result == null) {
            throw new IllegalArgumentException("No reader for " + frameType);
        }

        return result;
    }
}
