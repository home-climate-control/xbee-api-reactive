package com.homeclimatecontrol.xbee;

import com.rapplogic.xbee.api.XBeeRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FrameIdGeneratorTest {

    /**
     * Make sure we never get zero, or {@link com.rapplogic.xbee.api.XBeeRequest#DEFAULT_FRAME_ID}.
     */
    @Test
    void rollOverTest() {

        for (int i = 0; i < 0xFF * 4; i++) {

            var id = FrameIdGenerator.getInstance().getNext();

            assertThat(id).isNotZero();
            assertThat(id).isNotEqualTo(XBeeRequest.DEFAULT_FRAME_ID);
        }
    }
}
