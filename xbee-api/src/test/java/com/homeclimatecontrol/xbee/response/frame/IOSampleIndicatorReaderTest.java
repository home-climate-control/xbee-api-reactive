package com.homeclimatecontrol.xbee.response.frame;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

class IOSampleIndicatorReaderTest {

    @Test
    void parseDigitalMask0x01() {

        var payload = new byte[] { 0x00, 0x01 };
        var buffer = ByteBuffer.wrap(payload);
        var mask = IOSampleIndicatorReader.parseDigitalMask(buffer);

        assertThat(mask.get(0)).isTrue();

        for (var bit = 1; bit < 16; bit++) {
            assertThat(mask.get(bit)).isFalse();
        }
    }

    @Test
    void parseDigitalMask0x0100() {

        var payload = new byte[] { 0x01, 0x00 };
        var buffer = ByteBuffer.wrap(payload);
        var mask = IOSampleIndicatorReader.parseDigitalMask(buffer);

        assertThat(mask.get(8)).isTrue();

        for (var bit = 0; bit < 8; bit++) {
            assertThat(mask.get(bit)).isFalse();
        }

        for (var bit = 9; bit < mask.size(); bit++) {
            assertThat(mask.get(bit)).isFalse();
        }
    }

    @Test
    void parseDigitalMask0x1801() {

        var payload = new byte[] { 0x18, 0x01 };
        var buffer = ByteBuffer.wrap(payload);
        var mask = IOSampleIndicatorReader.parseDigitalMask(buffer);

        var i = mask.stream().iterator();

        assertThat(i.next()).isZero();
        assertThat(i.next()).isEqualTo(11);
        assertThat(i.next()).isEqualTo(12);
    }
}
