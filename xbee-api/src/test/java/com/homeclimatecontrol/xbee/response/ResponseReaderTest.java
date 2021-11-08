package com.homeclimatecontrol.xbee.response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ResponseReaderTest {

    private final Logger logger = LogManager.getLogger();

    @ParameterizedTest
    @MethodSource("goodNonEscapedFrameProvider")
    void checksum(byte[] packet){

        var buffer = Arrays.copyOfRange(packet, 3, packet.length - 1);
        var checksum = packet[packet.length - 1];
        var rr = new ResponseReader();

        assertThatCode(() -> {
            rr.verifyChecksum(checksum, buffer);
            // If we're made it this far, checksum is good
        }).doesNotThrowAnyException();
    }

    @Test
    void unknownType() throws IOException {

        var packet = new byte[] {
                0x7E, // Start delimiter
                0x00, // Length MSB
                0x05, // Length LSB

                // Frame data start
                0x03, // API Identifier - INVALID
                0x01, // API Frame ID
                0x4E, 0x4A, // AT Command (NJ)
                (byte) 0xFF, // value to set command to
                0x64 // Checksum
        };

        var buffer = new ByteArrayInputStream(packet, 1, packet.length - 1);
        var rr = new ResponseReader();

        assertThatIllegalArgumentException().isThrownBy(() -> rr.read(buffer)).withMessage("Unknown frame type 0x03");
    }

    @Test
    void noReader() throws IOException {

        var packet = new byte[] {
                0x7E, // Start delimiter
                0x00, // Length MSB
                0x05, // Length LSB

                // Frame data start
                (byte) 0xA3, // API Identifier - "Many-to-One Route Request Indicator", unlikely we'll deal with it
                0x01, // API Frame ID
                0x4E, 0x4A, // AT Command (NJ)
                (byte) 0xFF, // value to set command to
                (byte) 0xC4 // Checksum
        };

        var buffer = new ByteArrayInputStream(packet, 1, packet.length - 1);
        var rr = new ResponseReader();

        assertThatIllegalArgumentException().isThrownBy(() -> rr.read(buffer)).withMessage("No reader for {Frame type=0xA3 Many-to-One Route Request Indicator}");
    }

    @Test
    void localATCommandResponse() throws IOException {

        var packet = new byte[] {
                0x00, 0x07, // length
                (byte) 0x88, // Local AT command response
                0x01, // Frame ID
                0x48, 0x56, // HV
                0x00, // Status
                0x1A, 0x46, // Raw data
                0x78 // Checksum
        };

        var buffer = new ByteArrayInputStream(packet, 0, packet.length);
        var rr = new ResponseReader();

        assertThatCode(() -> {
            var response = rr.read(buffer);
            logger.info("AT response: {}", response);
        }).doesNotThrowAnyException();
    }

    @Test
    void api2CommandResponse() {

        var packet = new byte[] {
                0x00, 0x05, // length
                (byte) 0x88, // Local AT Command Response
                0x01, // Frame ID
                0x41, 0x50, // AP
                0x00, // Status
                (byte) 0xE5 // Checksum
        };

        var buffer = new ByteArrayInputStream(packet, 0, packet.length);
        var rr = new ResponseReader();

        assertThatCode(() -> {
            var response = rr.read(buffer);
            logger.info("AT response: {}", response);
        }).doesNotThrowAnyException();
    }

    @Test
    void api3CommandResponse() {

        var packet = new byte[] {
                0x00, 0x05, // length
                (byte) 0x88, // Local AT Command Response
                0x01, // Frame ID
                0x41, 0x50, // AP
                0x03, // Status
                (byte) 0xE2 // Checksum
        };

        var buffer = new ByteArrayInputStream(packet, 0, packet.length);
        var rr = new ResponseReader();

        assertThatCode(() -> {
            var response = rr.read(buffer);
            logger.info("AT response: {}", response);
        }).doesNotThrowAnyException();
    }

    @Test
    void ndCommandResponse() {

        var packet = new byte[] {
                0x00, 0x1D, // Length
                (byte) 0x88, // Local AT Command Response
                0x01, // Frame ID
                0x4E, 0x44, // ND
                0x00, 0x46, // Address 16?
                0x34, 0x00, 0x7D, 0x33, (byte) 0xA2, 0x00, 0x40, 0x2D, // Address 64?
                0x52, (byte) 0xDD, 0x50, 0x52, 0x4F, 0x42, 0x45, 0x00,
                (byte) 0xFF, (byte) 0xFE, // MY?
                0x01, 0x00, // SH?
                (byte) 0xC1, 0x05, 0x10, 0x1E, // SL?
                (byte) 0xAF // Checksum
        };

        var buffer = new ByteArrayInputStream(packet, 0, packet.length);
        var rr = new ResponseReader();

        assertThatCode(() -> {
            var response = rr.read(buffer);
            logger.info("ND response: {}", response);
        }).doesNotThrowAnyException();
    }

    /**
     * Non-escaped frame provider.
     *
     * See <a href="https://www.digi.com/resources/documentation/Digidocs/90002002/Default.htm#Reference/r_api_examples.htm">API Examples</a>
     *
     * @return Stream of known good packets (start delimiter included).
     */
    private static Stream<byte[]> goodNonEscapedFrameProvider() {

        var packet0 = new byte[] {
                0x7E, // Start delimiter
                0x00, // Length MSB
                0x0A, // Length LSB

                // Frame data start
                0x01, // API Identifier
                0x01, // API Frame ID
                0x50, 0x01, // Destination address low
                0x00, // Option byte
                0x48, 0x65, 0x6C, 0x6C, 0x6F, // Data packet
                (byte) 0xB8 // Checksum
        };

        var packet1 = new byte[] {
                0x7E, // Start delimiter
                0x00, // Length MSB
                0x05, // Length LSB

                // Frame data start
                0x08, // API Identifier (AT Command API)
                0x01, // API Frame ID
                0x4E, 0x4A, // AT Command (NJ)
                (byte) 0xFF, // value to set command to
                0x5F // Checksum
        };

        var packet2 = new byte[] {
                0x7E, // Start delimiter
                0x00, // Length MSB
                0x04, // Length LSB

                // Frame data start
                0x08, // API Identifier (AT Command API)
                0x01, // API Frame ID
                0x4E, 0x44, // AT Command (ND)
                0x64 // Checksum
        };

        var packet3 = new byte[] {
                0x7E, // Start delimiter
                0x00, // Length MSB
                0x10, // Length LSB

                // Frame data start
                0x17, // API Identifier (Remote Command API)
                0x01, // API Frame ID
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // Coordinator address
                (byte) 0xFF, (byte) 0xFE, // 16-bit destination address
                0x02, // Option byte: Apply Changes
                0x44, 0x31, // AT Command (D1)
                0x03, // Command parameter
                0x70 // Checksum
        };

        return Stream.of(packet0, packet1, packet2, packet3);
    }

    /**
     * Escaped frame provider.
     *
     * @return Stream of known good packets (start delimiter included).
     */
    private static Stream<byte[]> goodEscapedFrameProvider() {

        var packet0 = new byte[] {
                (byte) 0x7E, // Start delimiter
                (byte) 0x00, // Length MSB
                (byte) 0x0F, // Length LSB

                // Frame data start
                (byte) 0x17, // API Identifier ('Remote AT' command)
                (byte) 0x01, // API Frame ID
                (byte) 0x00,
                (byte) 0x7D, // 0x7D33 is 0x13 escaped
                (byte) 0x33,
                (byte) 0xA2,
                (byte) 0x00,
                (byte) 0x40,
                (byte) 0x62,
                (byte) 0xAC,
                (byte) 0x98,
                (byte) 0xFF,
                (byte) 0xFE,
                (byte) 0x02, // 0x02 means 'apply changes'
                (byte) 0x44,
                (byte) 0x30,
                (byte) 0xD9  // Checksum
        };

        return Stream.of(packet0);
    }
}
