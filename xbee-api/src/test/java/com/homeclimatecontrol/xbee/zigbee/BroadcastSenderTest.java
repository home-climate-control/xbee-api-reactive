package com.homeclimatecontrol.xbee.zigbee;

import com.homeclimatecontrol.xbee.XBeeReactive;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.zigbee.ZNetTxRequest;
import com.rapplogic.xbee.util.ByteUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.homeclimatecontrol.xbee.TestPortProvider.getCoordinatorTestPort;
import static org.assertj.core.api.Assertions.assertThatCode;

class BroadcastSenderTest {

    private final Logger logger = LogManager.getLogger();

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void broadcast() {
        assertThatCode(() -> {
            try (var xbee = new XBeeReactive(getCoordinatorTestPort())) {

                var payload = ByteUtils.stringToIntArray("42");
                var broadcast = new ZNetTxRequest(XBeeAddress64.BROADCAST, payload);
                broadcast.setOption(ZNetTxRequest.Option.BROADCAST);

                var ack = xbee.sendAsync(broadcast);

                // Now it is really sent
                ack.block();

                // We're done here
                logger.info("Broadcast sent: {}", broadcast);
            }
        }).doesNotThrowAnyException();
    }
}
