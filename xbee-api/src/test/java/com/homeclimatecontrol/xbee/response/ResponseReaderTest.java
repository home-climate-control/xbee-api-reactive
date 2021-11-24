package com.homeclimatecontrol.xbee.response;

import com.homeclimatecontrol.xbee.response.frame.XBeeResponseFrame;
import com.rapplogic.xbee.api.AtCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ResponseReaderTest {

    private final Logger logger = LogManager.getLogger();

    @ParameterizedTest
    @MethodSource("goodNonEscapedFrameProvider")
    void checksum(byte[] packet) {

        var buffer = Arrays.copyOfRange(packet, 3, packet.length - 1);
        var checksum = packet[packet.length - 1];
        var rr = new ResponseReader();

        assertThatCode(() -> {
            rr.verifyChecksum(checksum, buffer);
            // If we're made it this far, checksum is good
        }).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("goodEscapedFrameProvider")
    void checksumEscaped(byte[] packet) {

        var buffer = new ByteArrayInputStream(packet, 1, packet.length - 1);
        var rr = new ResponseReader();

        assertThatCode(() -> {
            rr.read(buffer);
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

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> rr.read(buffer)).withMessage("No reader for {Frame type=0xA3 Many-to-One Route Request Indicator}");
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
    void aiCommandResponse() {

        var packet = new byte[] {
                0x00,0x06, // Length
                (byte) 0x88, // Local AT Command Response
                0x01, // Frame ID
                0x41, 0x49, // AI
                0x00, // Status
                0x00, // Payload
                (byte) 0xec
        };

        checkResponse(packet, AtCommand.Command.AI);
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

        checkResponse(packet, AtCommand.Command.AP);
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

        checkResponse(packet, AtCommand.Command.AP);
    }

    @Test
    void d0CommandResponse() {

        var packet = new byte[] {
                0x00, 0x06, // Length
                (byte) 0x88, // Local AT Command Response
                0x01, // Frame ID
                0x44, 0x30, // D0
                0x00, 0x01, // Payload
                0x01 // Checksum
        };

        checkResponse(packet, AtCommand.Command.D0);
    }

    @Test
    void d2CommandResponse() {

        var packet = new byte[] {
                0x00, 0x06, // Length
                (byte) 0x88, // Local AT Command Response
                0x01, // Frame ID
                0x44, 0x32, // D2
                0x00, 0x00, // Payload
                0x00 // Checksum
        };

        checkResponse(packet, AtCommand.Command.D2);
    }

    @Test
    void d5CommandResponse() {

        var packet = new byte[] {
                0x00, 0x06, // Length
                (byte) 0x88, // Local AT Command Response
                0x01, // Frame ID
                0x44, 0x35, // D5
                0x00, 0x01, // Payload
                (byte) 0xFC // Checksum
        };

        checkResponse(packet, AtCommand.Command.D5);
    }

    @Test
    void d7CommandResponse() {

        var packet = new byte[] {
                0x00, 0x06, // Length
                (byte) 0x88, // Local AT Command Response
                0x01, // Frame ID
                0x44, 0x37, // D5
                0x00, 0x01, // Payload
                (byte) 0xFA // Checksum
        };

        checkResponse(packet, AtCommand.Command.D7);
    }

    @Test
    void ddCommandResponse() {

        var packet = new byte[] {
                0x00, 0x09, // Length
                (byte) 0x88, // Local AT Command Response
                0x01, // Frame ID
                0x44, 0x44, // DD
                0x00, // Status
                0x00, 0x03, 0x00, 0x00, // Payload
                (byte) 0xeb // Checksum
        };

        checkResponse(packet, AtCommand.Command.DD);
    }

    @Test
    void chCommandResponse() {

        var packet = new byte[] {
                0x00, 0x06, // Length
                (byte) 0x88, // Local AT Command Response
                0x01, // Frame ID
                0x43, 0x48, // CH
                0x00, // Status
                0x14, // Payload
                (byte) 0xd7 // Checksum
        };

        checkResponse(packet, AtCommand.Command.CH);
    }

    @Test
    void isCommandResponse() {

        var packet = new byte[] {
                0x00, 0x05, // Length
                (byte) 0x88, // Local AT Command Response
                0x01, // Frame ID
                0x49, 0x53, // IS
                0x01, // Status
                (byte) 0xD9 // Checksum
        };

        checkResponse(packet, AtCommand.Command.IS);
    }

    @Test
    void remoteIOSample() {

        var packet = new byte[] {
                0x00, 0x18, // Length
                (byte) 0x92,
                0x00, 0x7D, 0x33, (byte) 0xA2, 0x00, 0x40, 0x2D, 0x52, (byte) 0xDD, // Source address 64 (escaped)
                0x46, 0x34, // Source address 16
                0x01, // Receive options
                0x01, // Sample count
                0x18, 0x01, // Digital sample mask
                0x0E,  // Analog sample mask 0b1110 - AD1, AD2, AD3
                0x18, 0x01, // Digital samples
                0x02, 0x0D, // AD1 sample
                0x02, 0x0C, // AD2 sample
                0x02, 0x0C, // AD3 sample
                0x35 // Checksum
        };

        var buffer = new ByteArrayInputStream(packet, 0, packet.length);
        var rr = new ResponseReader();

        assertThatCode(() -> {
            var response = rr.read(buffer);
            logger.info("IS response: {}", response);
        }).doesNotThrowAnyException();
    }

    @Test
    void myCommandResponse() {

        var packet = new byte[] {
                0x00, 0x07, // Length
                (byte) 0x88, // Local AT Command Response
                0x01, // Frame ID
                0x4d,0x59,  // MY
                0x00, // Status
                0x00,0x00, // Payload
                (byte) 0xd0 // Checksum
        };

        checkResponse(packet, AtCommand.Command.MY);
    }

    @Test
    void ncCommandResponse() {

        var packet = new byte[] {
                0x00, 0x06, // Length
                (byte) 0x88, // Local AT Command Response
                0x01, // Frame ID
                0x4e,0x43, // NC
                0x00,0x0a, // Payload
                (byte) 0xdb // Checksum
        };

        checkResponse(packet, AtCommand.Command.NC);
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
                0x52, (byte) 0xDD, 0x50, 0x52, 0x4F, 0x42, 0x45, 0x00, // NI?
                (byte) 0xFF, (byte) 0xFE, // Parent address (always 0xFFFE)
                0x01, // Device type
                0x00, // Status
                (byte) 0xC1, 0x05, // Profile ID
                0x10, 0x1E, // Mfg ID
                (byte) 0xAF // Checksum
        };

        checkResponse(packet, AtCommand.Command.ND);
    }

    @Test
    void niCommandResponse() {

        var packet = new byte[] {
                0x00, 0x10, // Length
                (byte) 0x88, // Local AT Command Response
                0x01, // Frame ID
                0x4e, 0x49, // NI
                0x00, // Status
                0x43, 0x4f, 0x4f, 0x52, 0x44, 0x49, 0x4e, 0x41, 0x54, 0x4f, 0x52, // COORDINATOR
                (byte) 0x9b // Checksum
        };

        checkResponse(packet, AtCommand.Command.NI);
    }

    @Test
    void ntCommandResponse() {

        var packet = new byte[] {
                0x00, 0x07, // Length
                (byte) 0x88, // Local AT Command Response
                0x01, // Frame ID
                0x4E, 0x54, // NT
                0x00, // Status
                0x00, 0x3C, // Payload
                (byte) 0x98 // Checksum
        };

        checkResponse(packet, AtCommand.Command.NT);
    }

    @Test
    void p0CommandResponse() {

        var packet = new byte[] {
                0x00, 0x06, // Length
                (byte) 0x88, // Local AT Command Response
                0x01, // Frame ID
                0x50, 0x30, // P0
                0x00, // Status
                0x01, // Payload
                (byte) 0xf5
        };

        checkResponse(packet, AtCommand.Command.P0);
    }

    @Test
    void vrCommandResponse() {

        var packet = new byte[] {
                0x00, 0x07, // Length
                (byte) 0x88, // Local AT Command Response
                0x01, // Frame ID
                0x56, 0x52, // VR
                0x00, // Status
                0x21, 0x70, // Payload
                0x3d // Checksum
        };

        checkResponse(packet, AtCommand.Command.VR);
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
                0x48, 0x65, 0x6C, 0x6C, 0x6F, // Payload
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
                0x7E, // Start delimiter
                0x00, 0x22, // Length
                (byte) 0x88, // Local AT Command Response
                0x01, // Frame ID
                0x4E, 0x44, // ND
                0x00, (byte) 0xAE, // Address 16?
                0x38, 0x00, 0x13, (byte) 0xA2, 0x00, 0x40, 0x2D, 0x03, // Address 64?
                0x0D, 0x48, 0x56, 0x41, 0x43, 0x2D, 0x54, 0x52, 0x41, 0x4E, 0x45, 0x00, // NI?
                (byte) 0xFF, (byte) 0xFE, // Parent address (always 0xFFFE)
                0x01, // Device type
                0x00, // Status
                (byte) 0xC1, 0x05, // Profile ID
                0x10, 0x1E, // Mfg ID
                0x7D, // Escape
                0x31 // Checksum = 0x11 ^ 0x20
        };

        return Stream.of(packet0);
    }

    private XBeeResponseFrame checkResponse(byte[] source, AtCommand.Command command) {

        var buffer = new ByteArrayInputStream(source, 0, source.length);
        var rr = new ResponseReader();
        var responseHolder = new ArrayList<XBeeResponseFrame>(1);

        assertThatCode(() -> {
            var response = rr.read(buffer);
            logger.info("{} response: {}", command, response);
            responseHolder.add(response);

        }).doesNotThrowAnyException();

        return responseHolder.get(0);
    }

    @Test
    void issue18packetND() {

        var packet = new byte[] {
                0x00, 0x22, // Length
                (byte) 0x88, // Local AT Command Response
                0x01, // Frame ID
                0x4E, 0x44, // ND
                0x00, (byte) 0xAE, // Address 16?
                0x38, 0x00, 0x13, (byte) 0xA2, 0x00, 0x40, 0x2D, 0x03, // Address 64?
                0x0D, 0x48, 0x56, 0x41, 0x43, 0x2D, 0x54, 0x52, 0x41, 0x4E, 0x45, 0x00, // NI?
                (byte) 0xFF, (byte) 0xFE, // Parent address (always 0xFFFE)
                0x01, // Device type
                0x00, // Status
                (byte) 0xC1, 0x05, // Profile ID
                0x10, 0x1E, // Mfg ID
                0x7D, // Escape
                0x31 // Checksum = 0x11 ^ 0x20
        };

        checkResponse(packet, AtCommand.Command.ND);
    }

    @Test
    void issue18packetD0() {

        // See https://www.digi.com/resources/documentation/Digidocs/90002002/Content/Reference/r_frame_0x97.htm

        var packet = new byte[] {
                0x00, 0x0F,
                (byte) 0x97, // Remote AT Command Response
                0x01,
                0x00, 0x13, (byte) 0xA2, 0x00, 0x40, 0x55, 0x73, 0x0D, // Remote Address64
                (byte) 0xDC, (byte) 0xCF, // Remote Address16
                0x44, 0x30, // D0
                0x00, // Status
                0x7D, // Escape
                0x5E // Checksum = 0x7E ^ 0x20
        };

        checkResponse(packet, AtCommand.Command.D0);
    }

    @Test
    void isEscapeCharacter() {

        var rr = new ResponseReader();

        assertThat(rr.isEscaped((byte) 0x00)).isFalse();
        assertThat(rr.isEscaped((byte) 0xFF)).isFalse();

        assertThat(rr.isEscaped((byte) 0x11)).isTrue();
        assertThat(rr.isEscaped((byte) 0x13)).isTrue();
        assertThat(rr.isEscaped((byte) 0x7D)).isTrue();
        assertThat(rr.isEscaped((byte) 0x7E)).isTrue();
    }
}
