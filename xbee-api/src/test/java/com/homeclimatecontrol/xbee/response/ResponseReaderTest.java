package com.homeclimatecontrol.xbee.response;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;

class ResponseReaderTest {

    @ParameterizedTest
    @MethodSource("goodNonEscapedFrameProvider")
    void checksum(byte[] packet){

        var buffer = new ByteArrayInputStream(packet, 1, packet.length - 1);
        var rr = new ResponseReader();

        assertThatCode(() -> {
            rr.read(buffer);
            // If we're made it this far, checksum is good
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

        byte[] packet0 = new byte[] {
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

        byte[] packet1 = new byte[] {
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

        byte[] packet2 = new byte[] {
                0x7E, // Start delimiter
                0x00, // Length MSB
                0x04, // Length LSB

                // Frame data start
                0x08, // API Identifier (AT Command API)
                0x01, // API Frame ID
                0x4E, 0x44, // AT Command (ND)
                0x64 // Checksum
        };

        byte[] packet3 = new byte[] {
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

        byte[] packet0 = new byte[] {
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
