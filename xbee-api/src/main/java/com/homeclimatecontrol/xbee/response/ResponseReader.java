package com.homeclimatecontrol.xbee.response;

import com.homeclimatecontrol.xbee.FrameType;
import com.homeclimatecontrol.xbee.response.frame.FrameReader;
import com.homeclimatecontrol.xbee.response.frame.IOSampleIndicatorReader;
import com.homeclimatecontrol.xbee.response.frame.LocalATCommandResponseReader;
import com.homeclimatecontrol.xbee.response.frame.RemoteATCommandResponseReader;
import com.homeclimatecontrol.xbee.response.frame.XBeeResponseFrame;
import com.homeclimatecontrol.xbee.util.HexFormat;
import com.homeclimatecontrol.xbee.util.XbeeChecksum;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;

import static com.homeclimatecontrol.xbee.FrameType.IO_SAMPLE_INDICATOR;
import static com.homeclimatecontrol.xbee.FrameType.LOCAL_AT_COMMAND_RESPONSE;
import static com.homeclimatecontrol.xbee.FrameType.REMOTE_AT_COMMAND_RESPONSE;

/**
 * XBee response frame reader.
 *
 * See <a href="https://www.digi.com/resources/documentation/Digidocs/90002002/Content/Reference/r_api_frame_format_900hp.htm">API frame format</a>.
 *
 * @author Copyright &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2021
 */
public class ResponseReader {

    public static final byte FRAME_DELIMITER = 0x7E;
    private static final byte ESCAPE = 0x7D;

    private static final Map<FrameType, FrameReader> frame2reader = Map.of(
            LOCAL_AT_COMMAND_RESPONSE, new LocalATCommandResponseReader(),
            IO_SAMPLE_INDICATOR, new IOSampleIndicatorReader(),
            REMOTE_AT_COMMAND_RESPONSE, new RemoteATCommandResponseReader()
    );

    /**
     * Read the XBee response from the input stream.
     *
     * @param in Stream to read from. The stream pointer is expected to be at offset 1, right after the {@code 0x7E}
     *           start delimiter.
     * @return A newly instantiated response object.
     */
    public XBeeResponseFrame read(InputStream in) throws IOException {

        var headerBuffer = in.readNBytes(2);

        verifyRead(2, headerBuffer.length, "length");

        var frameSize = readLength(headerBuffer);
        var frameBuffer = readFrame(in, frameSize);
        var checksum = unescape(in);

        verifyChecksum(checksum, frameBuffer);

        return getReader(frameBuffer[0]).read(ByteBuffer.wrap(frameBuffer, 1, frameBuffer.length - 1));
    }

    /**
     * Read the frame, unescaping it along the way.
     *
     * See <a href="https://www.digi.com/resources/documentation/Digidocs/90001456-13/concepts/c_api_escaped_operating_mode.htm">API escaped operating mode (API 2)</a>
     */
    private byte[] readFrame(InputStream in, int frameSize) throws IOException {

        var buffer = new ByteArrayOutputStream();
        var leftToRead = frameSize;

        while (leftToRead > 0) {
            try {
                buffer.write(unescape(in));
            } catch (EOFException ex) {
                var offset = frameSize - leftToRead;
                throw new IOException("Unexpected EOF at frame offset "
                        + offset + " (" + HexFormat.format(offset)
                        + ") of " + frameSize + " (" + HexFormat.format(frameSize) + ")", ex);
            }
            leftToRead--;
        }

        return buffer.toByteArray();
    }

    private byte readByte(InputStream in) throws IOException {

        var b = in.read();

        if (b == -1) {
            throw new EOFException();
        }

        return (byte) (b & 0xFF);
    }

    private byte unescape(InputStream in) throws IOException {
        var b = readByte(in);

        if (b != ESCAPE) {
            return b;
        }

        var e = readByte(in);

        return (byte) (0x20 ^ e);
    }

    private int readLength(byte[] buffer) {
        return (buffer[0] << 8) + (buffer[1] & 0xFF);
    }

    private void verifyRead(int expected, int actual, String type) throws IOException {
        if (expected != actual) {
            throw new IOException("Read " + actual + " " + type + " bytes instead of " + expected + " expected");
        }
    }

    void verifyChecksum(byte checksumExpected, byte[] frameBuffer) {

        var payload = ByteBuffer.wrap(frameBuffer);
        var checksum = new XbeeChecksum();

        checksum.update(payload);

        var checksumActual = checksum.getValue();

        if (checksumActual != checksumExpected) {
            throw new IllegalArgumentException("Checksum mismatch, expected " + HexFormat.format(checksumExpected)
                    + ", actual " + HexFormat.format(checksumActual)
                    + ", data: " + HexFormat.format(ByteBuffer.wrap(frameBuffer)));
        }
    }

    private FrameReader getReader(byte type) {

        var frameType = FrameType.getByType(type);
        var result = frame2reader.get(frameType);

        if (result == null) {
            throw new UnsupportedOperationException("No reader for " + frameType);
        }

        return result;
    }
}
