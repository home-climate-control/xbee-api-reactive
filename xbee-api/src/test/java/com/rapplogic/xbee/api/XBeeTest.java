package com.rapplogic.xbee.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class XBeeTest {

    /**
     * Make sure {@link XBeeRequest#NO_RESPONSE_FRAME_ID} is never generated.
     */
    @Test
    void nextFrameId() {

        var xbee = new XBee();

        // Not 0; that would be the NO_RESPONSE_FRAME_ID
        assertThat(xbee.getNextFrameId()).isEqualTo(1);

        for (var count = 0; count < 0xFF + 1; count++) {
            assertThat(xbee.getNextFrameId()).isNotZero();
        }
        assertThat(xbee.getNextFrameId()).isEqualTo(3);
    }
}
